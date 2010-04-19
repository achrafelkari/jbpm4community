
    create table JBPM4_PROPERTY (
        KEY_ varchar2(255 char) not null,
        VERSION_ number(10,0) not null,
        VALUE_ varchar2(255 char),
        primary key (KEY_)
    );

    drop index IDX_HDETAIL_HVAR;

    drop index IDX_HDETAIL_HTASK;

    create index IDX_HDET_HVAR on JBPM4_HIST_DETAIL (HVAR_);

    create index IDX_HDET_HTASK on JBPM4_HIST_DETAIL (HTASK_);

    create index IDX_HSUPERT_SUB on JBPM4_HIST_TASK (SUPERTASK_);

