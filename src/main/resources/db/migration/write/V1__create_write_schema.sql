CREATE TABLE customers (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           full_name VARCHAR(150) NOT NULL,
                           email VARCHAR(180) NOT NULL UNIQUE,
                           document_number VARCHAR(20) NOT NULL UNIQUE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          customer_id UUID NOT NULL REFERENCES customers(id),
                          account_number VARCHAR(30) NOT NULL UNIQUE,
                          balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          version BIGINT NOT NULL DEFAULT 0,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT chk_account_balance CHECK (balance >= 0),
                          CONSTRAINT chk_account_status
                              CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);

CREATE TABLE cards (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       account_id UUID NOT NULL REFERENCES accounts(id),
                       last_four_digits CHAR(4) NOT NULL,
                       credit_limit NUMERIC(19, 2) NOT NULL,
                       available_limit NUMERIC(19, 2) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_card_limit CHECK (credit_limit >= 0),
                       CONSTRAINT chk_card_available_limit
                           CHECK (available_limit >= 0),
                       CONSTRAINT chk_card_status
                           CHECK (status IN ('ACTIVE', 'BLOCKED', 'CANCELLED'))
);

CREATE TABLE invoices (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          card_id UUID NOT NULL REFERENCES cards(id),
                          reference_month DATE NOT NULL,
                          total_amount NUMERIC(19, 2) NOT NULL,
                          paid_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
                          due_date DATE NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT chk_invoice_total CHECK (total_amount > 0),
                          CONSTRAINT chk_invoice_paid
                              CHECK (paid_amount >= 0 AND paid_amount <= total_amount),
                          CONSTRAINT chk_invoice_status
                              CHECK (status IN ('OPEN', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')),

                          CONSTRAINT uq_card_invoice_month
                              UNIQUE (card_id, reference_month)
);

CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          invoice_id UUID NOT NULL REFERENCES invoices(id),
                          account_id UUID NOT NULL REFERENCES accounts(id),
                          amount NUMERIC(19, 2) NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                          idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          completed_at TIMESTAMPTZ,

                          CONSTRAINT chk_payment_amount CHECK (amount > 0),
                          CONSTRAINT chk_payment_status
                              CHECK (status IN (
                                                'PENDING',
                                                'PROCESSING',
                                                'COMPLETED',
                                                'FAILED'
                                  ))
);

CREATE TABLE ledger_entries (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                account_id UUID NOT NULL REFERENCES accounts(id),
                                payment_id UUID REFERENCES payments(id),
                                entry_type VARCHAR(20) NOT NULL,
                                amount NUMERIC(19, 2) NOT NULL,
                                description VARCHAR(255) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                CONSTRAINT chk_ledger_amount CHECK (amount > 0),
                                CONSTRAINT chk_ledger_type
                                    CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               aggregate_id UUID NOT NULL,
                               aggregate_type VARCHAR(100) NOT NULL,
                               event_type VARCHAR(150) NOT NULL,
                               payload JSONB NOT NULL,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               published_at TIMESTAMPTZ,
                               attempts INTEGER NOT NULL DEFAULT 0,

                               CONSTRAINT chk_outbox_attempts CHECK (attempts >= 0)
);


CREATE INDEX idx_accounts_customer
    ON accounts(customer_id);

CREATE INDEX idx_cards_account
    ON cards(account_id);

CREATE INDEX idx_invoices_card_status
    ON invoices(card_id, status);

CREATE INDEX idx_invoices_due_date
    ON invoices(due_date);

CREATE INDEX idx_payments_invoice
    ON payments(invoice_id);

CREATE INDEX idx_payments_account
    ON payments(account_id);

CREATE INDEX idx_ledger_account_created
    ON ledger_entries(account_id, created_at DESC);

CREATE INDEX idx_outbox_pending
    ON outbox_events(created_at)
    WHERE published_at IS NULL;
