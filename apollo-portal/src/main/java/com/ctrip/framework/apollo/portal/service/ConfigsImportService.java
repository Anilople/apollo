package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.NamespaceDTO;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.component.PermissionValidator;
import com.ctrip.framework.apollo.portal.entity.model.NamespaceTextModel;
import com.ctrip.framework.apollo.portal.environment.Env;
import com.ctrip.framework.apollo.portal.util.ConfigFileUtils;
import com.ctrip.framework.apollo.portal.util.ConfigToFileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wxq
 */
@Service
public class ConfigsImportService {

  private final static Logger logger = LoggerFactory.getLogger(ConfigsImportService.class);

  private final AppService appService;

  private final ItemService itemService;

  private final NamespaceService namespaceService;

  private final PermissionValidator permissionValidator;

  public ConfigsImportService(
      final AppService appService,
      final ItemService itemService,
      final @Lazy NamespaceService namespaceService,
      PermissionValidator permissionValidator) {
    this.appService = appService;
    this.itemService = itemService;
    this.namespaceService = namespaceService;
    this.permissionValidator = permissionValidator;
  }

  /**
   * @param standardFilename get appId and namespace from it
   */
  public boolean hasModifyNamespacePermission(String env, String standardFilename) {
    final String appId = ConfigFileUtils.getAppId(standardFilename);
    final String namespace = ConfigFileUtils.getNamespace(standardFilename);
    if (permissionValidator.isAppAdmin(appId)) {
      return true;
    } else {
      return permissionValidator.hasModifyNamespacePermission(appId, namespace, env);
    }
  }

  /**
   * move from {@link com.ctrip.framework.apollo.portal.controller.ConfigsImportController}
   */
  public void importConfig(
      final String appId,
      final String env,
      final String clusterName,
      final String namespaceName,
      final long namespaceId,
      final String format,
      final String configText
  ) {
    final NamespaceTextModel model = new NamespaceTextModel();

    model.setAppId(appId);
    model.setEnv(env);
    model.setClusterName(clusterName);
    model.setNamespaceName(namespaceName);
    model.setNamespaceId(namespaceId);
    model.setFormat(format);
    model.setConfigText(configText);

    itemService.updateConfigItemByText(model);
  }

  /**
   * import one config from file
   */
  public void importOneConfigFromFile(
      final String appId,
      final String env,
      final String clusterName,
      final String namespaceName,
      final String configText,
      final String format
  ) {
    final NamespaceDTO namespaceDTO = namespaceService
        .loadNamespaceBaseInfo(appId, Env.valueOf(env), clusterName, namespaceName);
    this.importConfig(appId, env, clusterName, namespaceName, namespaceDTO.getId(), format, configText);
  }

  /**
   * import a config file.
   * the name of config file must be special like
   * appId+cluster+namespace.format
   * Example:
   * <pre>
   *   123456+default+application.properties (appId is 123456, cluster is default, namespace is application, format is properties)
   *   654321+north+password.yml (appId is 654321, cluster is north, namespace is password, format is yml)
   * </pre>
   * so we can get the information of appId, cluster, namespace, format from the file name.
   * @param env environment
   * @param standardFilename appId+cluster+namespace.format
   * @param configText config content
   */
  public void importOneConfigFromText(
      final String env,
      final String standardFilename,
      final String configText
  ) {
    final String appId = ConfigFileUtils.getAppId(standardFilename);
    final String clusterName = ConfigFileUtils.getClusterName(standardFilename);
    final String namespace = ConfigFileUtils.getNamespace(standardFilename);
    final String format = ConfigFileUtils.getFormat(standardFilename);
    this.importOneConfigFromFile(appId, env, clusterName, namespace, configText, format);
  }

  /**
   * @see ConfigsImportService#importOneConfigFromText(java.lang.String, java.lang.String, java.lang.String)
   * @throws AccessControlException if has no modify namespace permission
   */
  public void importOneConfigFromFile(
      final String env,
      final String standardFilename,
      final InputStream inputStream
  ) {
    // check permission
    if (!this.hasModifyNamespacePermission(env, standardFilename)) {
      throw new AccessControlException("no permission with " + standardFilename);
    }

    final String configText;
    try(InputStream in = inputStream) {
      configText = ConfigToFileUtils.fileToString(in);
    } catch (IOException e) {
      throw new ServiceException("Read config file errors:{}", e);
    }
    this.importOneConfigFromText(env, standardFilename, configText);
  }

  /**
   * catch all exception in method.
   * @param env environment
   * @param file file uploaded, maybe a zip file
   * @return import result. if success return 0, else return error message
   */
  public Object importOneConfigFromFileQuiet(
      final String env,
      final MultipartFile file
  ) {
    final String originalFilename = file.getOriginalFilename();
    try {
      ConfigFileUtils.check(file);
      importOneConfigFromFile(env, originalFilename, file.getInputStream());
    } catch (Exception e) {
      logger.debug("import " + originalFilename + " fail. ", e);
      return "fail. " + e.getMessage();
    }
    return "0";
  }
}
