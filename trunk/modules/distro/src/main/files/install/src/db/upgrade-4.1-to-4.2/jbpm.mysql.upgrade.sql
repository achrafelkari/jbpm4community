
    create table JBPM4_PROPERTY (
        KEY_ varchar(255) not null,
        VERSION_ integer not null,
        VALUE_ varchar(255),
        primary key (KEY_)
    ) type=InnoDB;

    alter table JBPM4_HIST_DETAIL drop index IDX_HDETAIL_HACTI;

    alter table JBPM4_HIST_DETAIL drop index IDX_HDETAIL_HPROCI;

    alter table JBPM4_HIST_DETAIL drop index IDX_HDETAIL_HVAR;

    alter table JBPM4_HIST_DETAIL drop index IDX_HDETAIL_HTASK;
    
    create index IDX_HSUPERT_SUB on JBPM4_HIST_TASK (SUPERTASK_);
