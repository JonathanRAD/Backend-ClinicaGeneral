-- ═══════════════════════════════════════════════════════════════
-- SEED: Datos de prueba para el módulo de Inventario
-- Clínica Bienestar
-- 10 Medicamentos · 5 Lotes · 2 Proveedores
-- Fecha base: Abril 2026
-- ═══════════════════════════════════════════════════════════════

-- ────────────────────────────────────────────────────────────────
-- 1. PROVEEDORES (2 nuevos)
-- ────────────────────────────────────────────────────────────────
INSERT INTO public.proveedores (nombre, ruc, telefono, email, direccion, contacto)
VALUES
  ('LABORATORIOS ROEMMERS', '20100114349', '+51 1 411 5200', 'ventas@roemmers.com.pe',
   'Av. Guardia Civil 1321, San Isidro, Lima', 'Carlos Mendoza'),
  ('DROGUERÍA DECO S.A.C.', '20512345678', '+51 1 500 3000', 'pedidos@deco.com.pe',
   'Jr. Ucayali 395, Cercado de Lima', 'Ana Torres')
ON CONFLICT (ruc) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- 2. MEDICAMENTOS (10 productos)
--    stock_minimo variado para probar alertas del semáforo
-- ────────────────────────────────────────────────────────────────
INSERT INTO public.medicamentos (codigo, nombre, descripcion, forma_farmaceutica, concentracion, precio_unitario, estado, stock_minimo)
VALUES
  ('MED-001', 'AMOXICILINA',
   'Antibiótico de amplio espectro, familia penicilinas',
   'CÁPSULA', '500 mg', 0.85, 'ACTIVO', 10),

  ('MED-002', 'IBUPROFENO',
   'Antiinflamatorio no esteroideo (AINE)',
   'TABLETA', '400 mg', 0.45, 'ACTIVO', 15),

  ('MED-003', 'OMEPRAZOL',
   'Inhibidor de la bomba de protones, protector gástrico',
   'CÁPSULA', '20 mg', 0.60, 'ACTIVO', 10),

  ('MED-004', 'METFORMINA',
   'Antidiabético oral, control de glucemia',
   'TABLETA', '850 mg', 0.55, 'ACTIVO', 20),

  ('MED-005', 'LOSARTÁN',
   'Antagonista de receptores de angiotensina II, antihipertensivo',
   'TABLETA', '50 mg', 0.70, 'ACTIVO', 10),

  ('MED-006', 'PARACETAMOL',
   'Analgésico y antipirético de primera línea',
   'JARABE', '120 mg/5 ml', 5.50, 'ACTIVO', 8),

  ('MED-007', 'CETIRIZINA',
   'Antihistamínico de segunda generación',
   'TABLETA', '10 mg', 0.35, 'ACTIVO', 10),

  ('MED-008', 'DEXAMETASONA',
   'Corticosteroide antiinflamatorio e inmunosupresor',
   'INYECTABLE', '4 mg/ml', 3.20, 'ACTIVO', 5),

  ('MED-009', 'SALBUTAMOL',
   'Broncodilatador beta-2 agonista de acción corta',
   'INHALADOR', '100 mcg/dosis', 18.00, 'ACTIVO', 5),

  ('MED-010', 'CLONAZEPAM',
   'Benzodiacepina anticonvulsivante y ansiolítico',
   'GOTAS', '2.5 mg/ml', 8.50, 'ACTIVO', 5)
ON CONFLICT (codigo) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- 3. LOTES (5 lotes con distintos escenarios)
--
--    🔴 Lote 1: VENCIDO (venció hace 29 días)
--    🔴 Lote 2: VENCE MUY PRONTO (en 5 días)
--    🟡 Lote 3: POR VENCER (en 40 días)
--    🟢 Lote 4: VIGENTE (vence en 8 meses)
--    🔴 Lote 5: STOCK AGOTADO + por vencer
--
--    Los IDs de medicamento se resuelven con subqueries
--    Los IDs de proveedor se resuelven con subqueries
-- ────────────────────────────────────────────────────────────────

-- 🔴 Lote 1: Amoxicilina — VENCIDO (29 días atrás), stock bajo
INSERT INTO public.lote_medicamentos (numero_lote, stock, fecha_vencimiento, fecha_ingreso, medicamento_id, proveedor_id)
VALUES (
  'LOT-2025-0187',
  3,
  CURRENT_DATE - INTERVAL '29 days',          -- ya venció
  CURRENT_TIMESTAMP - INTERVAL '6 months',    -- ingresó hace 6 meses
  (SELECT id FROM public.medicamentos WHERE codigo = 'MED-001' LIMIT 1),
  (SELECT id FROM public.proveedores WHERE ruc = '20100823550' LIMIT 1)  -- FARMINDUSTRIA
);

-- 🔴 Lote 2: Ibuprofeno — VENCE EN 5 DÍAS, stock medio
INSERT INTO public.lote_medicamentos (numero_lote, stock, fecha_vencimiento, fecha_ingreso, medicamento_id, proveedor_id)
VALUES (
  'LOT-2025-0342',
  45,
  CURRENT_DATE + INTERVAL '5 days',           -- vence en 5 días (peligro)
  CURRENT_TIMESTAMP - INTERVAL '11 months',
  (SELECT id FROM public.medicamentos WHERE codigo = 'MED-002' LIMIT 1),
  (SELECT id FROM public.proveedores WHERE ruc = '20107034540' LIMIT 1)  -- IQ FARMA
);

-- 🟡 Lote 3: Omeprazol — POR VENCER en 40 días, stock OK
INSERT INTO public.lote_medicamentos (numero_lote, stock, fecha_vencimiento, fecha_ingreso, medicamento_id, proveedor_id)
VALUES (
  'LOT-2026-0021',
  120,
  CURRENT_DATE + INTERVAL '40 days',          -- vence en 40 días (advertencia)
  CURRENT_TIMESTAMP - INTERVAL '4 months',
  (SELECT id FROM public.medicamentos WHERE codigo = 'MED-003' LIMIT 1),
  (SELECT id FROM public.proveedores WHERE ruc = '20100085284' LIMIT 1)  -- MEDIFARMA
);

-- 🟢 Lote 4: Paracetamol Jarabe — VIGENTE, vence en 8 meses
INSERT INTO public.lote_medicamentos (numero_lote, stock, fecha_vencimiento, fecha_ingreso, medicamento_id, proveedor_id)
VALUES (
  'LOT-2026-0099',
  200,
  CURRENT_DATE + INTERVAL '8 months',         -- muy lejano, sin riesgo
  CURRENT_TIMESTAMP - INTERVAL '2 months',
  (SELECT id FROM public.medicamentos WHERE codigo = 'MED-006' LIMIT 1),
  (SELECT id FROM public.proveedores WHERE ruc = '20100114349' LIMIT 1)  -- ROEMMERS
);

-- 🔴 Lote 5: Dexametasona — STOCK AGOTADO + vence en 22 días
INSERT INTO public.lote_medicamentos (numero_lote, stock, fecha_vencimiento, fecha_ingreso, medicamento_id, proveedor_id)
VALUES (
  'LOT-2026-0055',
  0,
  CURRENT_DATE + INTERVAL '22 days',          -- vence pronto y sin stock
  CURRENT_TIMESTAMP - INTERVAL '3 months',
  (SELECT id FROM public.medicamentos WHERE codigo = 'MED-008' LIMIT 1),
  (SELECT id FROM public.proveedores WHERE ruc = '20512345678' LIMIT 1)  -- DECO
);

-- ═══════════════════════════════════════════════════════════════
-- RESUMEN DE DATOS INSERTADOS:
-- ─────────────────────────────────────────────────────────────
-- 
-- PROVEEDORES (2):
--   • LABORATORIOS ROEMMERS
--   • DROGUERÍA DECO S.A.C.
--
-- MEDICAMENTOS (10):
--   MED-001  AMOXICILINA       Cápsula    500 mg
--   MED-002  IBUPROFENO        Tableta    400 mg
--   MED-003  OMEPRAZOL         Cápsula    20 mg
--   MED-004  METFORMINA        Tableta    850 mg
--   MED-005  LOSARTÁN          Tableta    50 mg
--   MED-006  PARACETAMOL       Jarabe     120 mg/5 ml
--   MED-007  CETIRIZINA        Tableta    10 mg
--   MED-008  DEXAMETASONA      Inyectable 4 mg/ml
--   MED-009  SALBUTAMOL        Inhalador  100 mcg/dosis
--   MED-010  CLONAZEPAM        Gotas      2.5 mg/ml
--
-- LOTES (5):
--   🔴 LOT-2025-0187  Amoxicilina    3 uds   VENCIDO (-29 días)
--   🔴 LOT-2025-0342  Ibuprofeno    45 uds   Vence en 5 días
--   🟡 LOT-2026-0021  Omeprazol    120 uds   Vence en 40 días
--   🟢 LOT-2026-0099  Paracetamol  200 uds   Vence en 8 meses
--   🔴 LOT-2026-0055  Dexametasona   0 uds   AGOTADO + vence en 22 días
--
-- PRODUCTOS SIN LOTES (stock 0 implícito):
--   MED-004 Metformina, MED-005 Losartán, MED-007 Cetirizina,
--   MED-009 Salbutamol, MED-010 Clonazepam
-- ═══════════════════════════════════════════════════════════════
