CREATE TABLE customer_dashboard (
                                    customer_id UUID PRIMARY KEY,
                                    full_name VARCHAR(150) NOT NULL,
                                    email VARCHAR(180) NOT NULL,
                                    total_accounts INTEGER NOT NULL DEFAULT 0,
                                    total_cards INTEGER NOT NULL DEFAULT 0,
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE account_balance_view (
                                      account_id UUID PRIMARY KEY,
                                      customer_id UUID NOT NULL,
                                      account_number VARCHAR(30) NOT NULL,
                                      balance NUMERIC(19, 2) NOT NULL,
                                      account_status VARCHAR(20) NOT NULL,
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE card_invoice_view (
                                   invoice_id UUID PRIMARY KEY,
                                   card_id UUID NOT NULL,
                                   customer_id UUID NOT NULL,
                                   last_four_digits CHAR(4) NOT NULL,
                                   reference_month DATE NOT NULL,
                                   total_amount NUMERIC(19, 2) NOT NULL,
                                   paid_amount NUMERIC(19, 2) NOT NULL,
                                   remaining_amount NUMERIC(19, 2) NOT NULL,
                                   due_date DATE NOT NULL,
                                   invoice_status VARCHAR(20) NOT NULL,
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payment_history_view (
                                      payment_id UUID PRIMARY KEY,
                                      invoice_id UUID NOT NULL,
                                      account_id UUID NOT NULL,
                                      customer_id UUID NOT NULL,
                                      amount NUMERIC(19, 2) NOT NULL,
                                      payment_status VARCHAR(20) NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL,
                                      completed_at TIMESTAMPTZ,
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for frequent queries

CREATE INDEX idx_dashboard_email
    ON customer_dashboard(email);

CREATE INDEX idx_account_view_customer
    ON account_balance_view(customer_id);

CREATE INDEX idx_invoice_view_customer_status
    ON card_invoice_view(customer_id, invoice_status);

CREATE INDEX idx_invoice_view_due_date
    ON card_invoice_view(due_date);

CREATE INDEX idx_payment_history_customer
    ON payment_history_view(customer_id, created_at DESC);

CREATE INDEX idx_payment_history_invoice
    ON payment_history_view(invoice_id);
