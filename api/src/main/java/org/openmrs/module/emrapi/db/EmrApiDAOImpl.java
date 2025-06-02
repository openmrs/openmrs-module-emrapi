package org.openmrs.module.emrapi.db;

import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.api.db.hibernate.DbSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EmrApiDAOImpl implements EmrApiDAO {
   
   protected final Log log = LogFactory.getLog(getClass());
   
   @Setter
   private DbSessionFactory sessionFactory;
    
    @Override
    public <T> List<T> executeHql(String queryString, Map<String, Object> parameters, Class<T> clazz) {
        Query query = createQuery(queryString, parameters);
        return query.list();
    }
    
    @Override
    public <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, Class<T> clazz) {
        String hql = loadHqlFromResource(resource);
        return executeHql(hql, parameters, clazz);
    }
    
    @Override
    public <T> List<T> executeHql(String hql, Map<String, Object> parameters, int startIndex, int pageSize) {
        Query query = createQuery(hql, parameters);
        query.setFirstResult(startIndex);
        query.setMaxResults(pageSize);
        return query.list();
    }
    
    @Override
    public <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, int startIndex, int pageSize) {
        String hql = loadHqlFromResource(resource);
        return executeHql(hql, parameters, startIndex, pageSize);
    }
    
    private Query createQuery(String hql, Map<String, Object> parameters) {
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                query.setParameterList(entry.getKey(), (Collection<?>) value);
            } else {
                query.setParameter(entry.getKey(), value);
            }
        }
        return query;
    }
    
    private String loadHqlFromResource(String resource) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is != null) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            } else {
                throw new RuntimeException("No resource found for " + resource);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + resource, e);
        }
    }
}
