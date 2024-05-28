package org.openmrs.module.emrapi.db;

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

public class EmrApiDAOImpl implements EmrApiDAO {

   protected final Log log = LogFactory.getLog(getClass());

   private DbSessionFactory sessionFactory;

   public void setSessionFactory(DbSessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> List<T> executeHql(String queryString, Map<String, Object> parameters, Class<T> clazz) {
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      for (String parameter : parameters.keySet()) {
         Object value = parameters.get(parameter);
         if (value instanceof Collection) {
            query.setParameterList(parameter, (Collection) value);
         }
         else {
            query.setParameter(parameter, value);
         }
      }
      return query.list();
   }

   @Override
   public <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, Class<T> clazz) {
      String hql = null;
      try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
         if (is != null) {
            hql = IOUtils.toString(is, StandardCharsets.UTF_8);
         }
      }
      catch (IOException e) {
         throw new RuntimeException("Error loading " + resource, e);
      }
      if (hql == null) {
         throw new RuntimeException("No resource found for " + resource);
      }
      return executeHql(hql, parameters, clazz);
   }
}
