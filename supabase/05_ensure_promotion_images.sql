-- Parche v7: asegura la columna para guardar la fotografía del combo/promoción.
-- Es seguro ejecutarlo aunque la columna ya exista.
ALTER TABLE public.promotions
ADD COLUMN IF NOT EXISTS image_url VARCHAR(800);
