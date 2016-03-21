package com.marklogic.appdeployer.command;

public abstract class SortOrderConstants {

    public static Integer DEPLOY_GROUPS = 5;

    public static Integer DEPLOY_PRIVILEGES = 10;
    public static Integer DEPLOY_ROLES = 20;
    public static Integer DEPLOY_USERS = 30;
    public static Integer DEPLOY_CERTIFICATE_TEMPLATES = 50;
    public static Integer GENERATE_TEMPORARY_CERTIFICATE = 55;
    public static Integer DEPLOY_CERTIFICATE_AUTHORITIES = 60;
    public static Integer DEPLOY_EXTERNAL_SECURITY = 70;
    public static Integer DEPLOY_PROTECTED_COLLECTIONS = 80;
    public static Integer DEPLOY_MIMETYPES = 90;
    
    public static Integer DEPLOY_TRIGGERS_DATABASE = 100;
    public static Integer DEPLOY_SCHEMAS_DATABASE = 100;
    public static Integer DEPLOY_CONTENT_DATABASES = 120;
    public static Integer DEPLOY_OTHER_DATABASES = 130;
    public static Integer DEPLOY_FORESTS = 150;

    public static Integer DEPLOY_REST_API_SERVERS = 200;
    public static Integer UPDATE_REST_API_SERVERS = 250;
    public static Integer DEPLOY_OTHER_SERVERS = 300;

    // Module code may depend on schemas, but not vice-versa.
    public static Integer LOAD_SCHEMAS = 350;
    
    // Modules have to be loaded after the REST API server has been updated, for if the deployer is expecting to load
    // modules via SSL, then the REST API server must already be configured with a certificate template
    public static Integer LOAD_MODULES = 400;

    // The modules database must exist before we deploy amps
    public static Integer DEPLOY_AMPS = 450;

    public static Integer DEPLOY_TRIGGERS = 700;
    
    public static Integer DEPLOY_SCHEDULED_TASKS = 800;

    public static Integer DEPLOY_DEFAULT_PIPELINES = 900;
    public static Integer DEPLOY_PIPELINES = 905;
    public static Integer DEPLOY_DOMAINS = 910;
    public static Integer DEPLOY_CPF_CONFIGS = 920;

    public static Integer DEPLOY_ALERT_CONFIGS = 950;
    public static Integer DEPLOY_ALERT_ACTIONS = 960;
    public static Integer DEPLOY_ALERT_RULES = 970;

    public static Integer DEPLOY_FLEXREP_CONFIGS = 1000;
    public static Integer DEPLOY_FLEXREP_TARGETS = 1010;
    
    public static Integer DEPLOY_SQL_VIEWS = 1100;

    public static Integer DEPLOY_FOREST_REPLICAS = 1200;
    
    // Undo constants
    public static Integer DELETE_GROUPS = 10000;

    public static Integer DELETE_MIMETYPES = 9500;
    
    public static Integer DELETE_USERS = 9000;
    public static Integer DELETE_CERTIFICATE_TEMPLATES = 9010;
    public static Integer DELETE_CERTIFICATE_AUTHORITIES = 9020;
    public static Integer DELETE_EXTERNAL_SECURITY = 9030;
    public static Integer DELETE_PROTECTED_COLLECTIONS = 9040;
    // Amps can reference roles, so must delete amps first
    public static Integer DELETE_AMPS = 9050;
    // Roles can reference privileges, so must delete roles first
    public static Integer DELETE_ROLES = 9060;
    public static Integer DELETE_PRIVILEGES = 9070;

    /*
     * This executes before databases are deleted, as deleting databases normally deletes the primary forests, so we
     * need to make sure the replicas are deleted first.
     */
    public static Integer DELETE_FOREST_REPLICAS = 8000;
    
    public static Integer DELETE_CONTENT_DATABASES = 8100;
    public static Integer DELETE_OTHER_DATABASES = 8120;
    public static Integer DELETE_TRIGGERS_DATABASE = 8140;
    public static Integer DELETE_SCHEMAS_DATABASE = 8160;

    public static Integer DELETE_REST_API_SERVERS = 7000;
    public static Integer DELETE_OTHER_SERVERS = 7010;

    public static Integer DELETE_SCHEDULED_TASKS = 1000;
}
