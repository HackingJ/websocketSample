
package duck.moon.sample.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.FrameworkServlet;

//this class is modified from org.springframework.web.socket.server.endpoint.SpringConfigurator
//this configurator make new endpoint when endpoint does not exist in spring bean.
//however, bring endpoint from spring container when endpoint bean is exist in spring container
public class MdConfigurator extends Configurator {
  private static Logger logger = LoggerFactory.getLogger(MdConfigurator.class);

  private static final Map<String, Map<Class<?>, String>> cache =
      new ConcurrentHashMap<String, Map<Class<?>, String>>();

  private static final String NO_VALUE = ObjectUtils.identityToString(new Object());

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    //this is servlet container application context
    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
    
    if (wac == null) {
      String message = "Failed to find the root WebApplicationContext. Was ContextLoaderListener not used?";
      logger.error(message);
      throw new IllegalStateException(message);
    }

    //obtain spring application context
    WebApplicationContext springWac = WebApplicationContextUtils.getWebApplicationContext(wac.getServletContext(), FrameworkServlet.SERVLET_CONTEXT_PREFIX + "appServlet");

    String beanName = ClassUtils.getShortNameAsProperty(endpointClass);
    if (springWac.containsBean(beanName)) {
      T endpoint = springWac.getBean(beanName, endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    Component annot = AnnotationUtils.findAnnotation(endpointClass, Component.class);
    if ((annot != null) && springWac.containsBean(annot.value())) {
      T endpoint = springWac.getBean(annot.value(), endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    beanName = getBeanNameByType(springWac, endpointClass);
    if (beanName != null) {
      return (T) springWac.getBean(beanName);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Creating new @ServerEndpoint instance of type " + endpointClass);
    }
    return springWac.getAutowireCapableBeanFactory().createBean(endpointClass);
  }

  private String getBeanNameByType(WebApplicationContext wac, Class<?> endpointClass) {

    String wacId = wac.getId();

    Map<Class<?>, String> beanNamesByType = cache.get(wacId);
    if (beanNamesByType == null) {
      beanNamesByType = new ConcurrentHashMap<Class<?>, String>();
      cache.put(wacId, beanNamesByType);
    }

    if (!beanNamesByType.containsKey(endpointClass)) {
      String[] names = wac.getBeanNamesForType(endpointClass);
      if (names.length == 1) {
        beanNamesByType.put(endpointClass, names[0]);
      }
      else {
        beanNamesByType.put(endpointClass, NO_VALUE);
        if (names.length > 1) {
          String message = "Found multiple @ServerEndpoint's of type " + endpointClass + ", names=" + names;
          logger.error(message);
          throw new IllegalStateException(message);
        }
      }
    }

    String beanName = beanNamesByType.get(endpointClass);
    return NO_VALUE.equals(beanName) ? null : beanName;
  }

}