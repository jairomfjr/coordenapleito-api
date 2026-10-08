-- SECOFI: totais item 15 por município no demonstrativo (BE, PAIF, PAEFI)

ALTER TABLE public.demonstrativo_ano_municipio
    ADD COLUMN IF NOT EXISTS total_be_item_15 DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS total_paif_item_15 DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS total_paefi_item_15 DOUBLE PRECISION;
