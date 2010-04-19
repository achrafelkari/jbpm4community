
    alter table JBPM4_DEPLOYPROP 
        drop constraint FK_DEPLPROP_DEPL;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_PARENT;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_SUBPI;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_INSTANCE;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_SUPEREXEC;

    alter table JBPM4_HIST_ACTINST 
        drop constraint FK_HACTI_HPROCI;

    alter table JBPM4_HIST_ACTINST 
        drop constraint FK_HTI_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HPROCI;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HACTI;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HVAR;

    alter table JBPM4_HIST_TASK 
        drop constraint FK_HSUPERT_SUB;

    alter table JBPM4_HIST_VAR 
        drop constraint FK_HVAR_HPROCI;

    alter table JBPM4_HIST_VAR 
        drop constraint FK_HVAR_HTASK;

    alter table JBPM4_ID_GROUP 
        drop constraint FK_GROUP_PARENT;

    alter table JBPM4_ID_MEMBERSHIP 
        drop constraint FK_MEM_GROUP;

    alter table JBPM4_ID_MEMBERSHIP 
        drop constraint FK_MEM_USER;

    alter table JBPM4_JOB 
        drop constraint FK_JOB_CFG;

    alter table JBPM4_LOB 
        drop constraint FK_LOB_DEPLOYMENT;

    alter table JBPM4_PARTICIPATION 
        drop constraint FK_PART_SWIMLANE;

    alter table JBPM4_PARTICIPATION 
        drop constraint FK_PART_TASK;

    alter table JBPM4_SWIMLANE 
        drop constraint FK_SWIMLANE_EXEC;

    alter table JBPM4_TASK 
        drop constraint FK_TASK_SWIML;

    alter table JBPM4_TASK 
        drop constraint FK_TASK_SUPERTASK;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_LOB;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_EXECUTION;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_EXESYS;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_TASK;

    drop table JBPM4_DEPLOYMENT if exists;

    drop table JBPM4_DEPLOYPROP if exists;

    drop table JBPM4_EXECUTION if exists;

    drop table JBPM4_HIST_ACTINST if exists;

    drop table JBPM4_HIST_DETAIL if exists;

    drop table JBPM4_HIST_PROCINST if exists;

    drop table JBPM4_HIST_TASK if exists;

    drop table JBPM4_HIST_VAR if exists;

    drop table JBPM4_ID_GROUP if exists;

    drop table JBPM4_ID_MEMBERSHIP if exists;

    drop table JBPM4_ID_USER if exists;

    drop table JBPM4_JOB if exists;

    drop table JBPM4_LOB if exists;

    drop table JBPM4_PARTICIPATION if exists;

    drop table JBPM4_PROPERTY if exists;

    drop table JBPM4_SWIMLANE if exists;

    drop table JBPM4_TASK if exists;

    drop table JBPM4_VARIABLE if exists;
