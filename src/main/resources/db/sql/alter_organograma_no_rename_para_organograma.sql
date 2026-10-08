DO $$
DECLARE
    v_schema TEXT := current_schema();
BEGIN
    IF to_regclass(format('%I.organograma_no', v_schema)) IS NOT NULL
       AND to_regclass(format('%I.organograma', v_schema)) IS NULL THEN
        EXECUTE format('ALTER TABLE %I.organograma_no RENAME TO organograma', v_schema);
    END IF;

    IF to_regclass(format('%I.organograma_no_id_seq', v_schema)) IS NOT NULL
       AND to_regclass(format('%I.organograma_id_seq', v_schema)) IS NULL THEN
        EXECUTE format('ALTER SEQUENCE %I.organograma_no_id_seq RENAME TO organograma_id_seq', v_schema);
    END IF;

    IF to_regclass(format('%I.organograma', v_schema)) IS NOT NULL THEN
        EXECUTE format(
            'ALTER TABLE %I.organograma ALTER COLUMN id SET DEFAULT nextval(%L)',
            v_schema,
            format('%I.organograma_id_seq', v_schema)
        );
    END IF;

    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pk_organograma_no') THEN
        EXECUTE format('ALTER TABLE %I.organograma RENAME CONSTRAINT pk_organograma_no TO pk_organograma', v_schema);
    END IF;

    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_organograma_no_codigo') THEN
        EXECUTE format('ALTER TABLE %I.organograma RENAME CONSTRAINT uq_organograma_no_codigo TO uq_organograma_codigo', v_schema);
    END IF;

    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_organograma_no_parent') THEN
        EXECUTE format('ALTER TABLE %I.organograma RENAME CONSTRAINT fk_organograma_no_parent TO fk_organograma_parent', v_schema);
    END IF;

    IF to_regclass(format('%I.idx_organograma_no_parent', v_schema)) IS NOT NULL
       AND to_regclass(format('%I.idx_organograma_parent', v_schema)) IS NULL THEN
        EXECUTE format('ALTER INDEX %I.idx_organograma_no_parent RENAME TO idx_organograma_parent', v_schema);
    END IF;

    IF to_regclass(format('%I.idx_organograma_no_nome', v_schema)) IS NOT NULL
       AND to_regclass(format('%I.idx_organograma_nome', v_schema)) IS NULL THEN
        EXECUTE format('ALTER INDEX %I.idx_organograma_no_nome RENAME TO idx_organograma_nome', v_schema);
    END IF;

    IF to_regclass(format('%I.idx_organograma_no_tipo', v_schema)) IS NOT NULL
       AND to_regclass(format('%I.idx_organograma_tipo', v_schema)) IS NULL THEN
        EXECUTE format('ALTER INDEX %I.idx_organograma_no_tipo RENAME TO idx_organograma_tipo', v_schema);
    END IF;
END $$;
