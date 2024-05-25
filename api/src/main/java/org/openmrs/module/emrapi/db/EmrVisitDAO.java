package org.openmrs.module.emrapi.db;

import java.util.List;
import java.util.Map;

public interface EmrVisitDAO {

    <T> List<T> executeHql(String queryString, Map<String, Object> parameters, Class<T> clazz);

    <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, Class<T> clazz);
}
