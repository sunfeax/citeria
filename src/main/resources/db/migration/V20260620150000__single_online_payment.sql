-- Payment is mocked through the appointment lifecycle (POST /appointments/{id}/pay),
-- so the standalone payments table is dropped. With a single payment method the
-- payment_method column carries no information and is removed (dropping it also drops
-- the dependent appointments_payment_method_chk constraint).

DROP TABLE payments;

ALTER TABLE appointments DROP COLUMN payment_method;
