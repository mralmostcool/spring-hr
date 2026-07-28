---- employee: soft-delete support
ALTER TABLE employee
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

---- types
CREATE TYPE certificate_status
AS ENUM (
    'SUBMITTED',
    'LEVEL_1',
    'LEVEL_2',
    'APPROVED',
    'REJECTED'
);

CREATE TYPE certificate_action_type
AS ENUM (
    'APPROVE',
    'REJECT'
);

---- certificate_state
CREATE TABLE certificate_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    candidate_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL DEFAULT 'Participation Certificate',
    status certificate_status NOT NULL DEFAULT 'SUBMITTED',

    current_designation_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_certificate_state_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES candidate (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_certificate_state_current_designation
        FOREIGN KEY (current_designation_id)
        REFERENCES designation (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_certificate_state_designation_consistency
        CHECK (
            (status IN ('APPROVED', 'REJECTED') AND current_designation_id IS NULL)
            OR
            (status IN ('SUBMITTED', 'LEVEL_1', 'LEVEL_2') AND current_designation_id IS NOT NULL)
        )
);

---- certificate_action
CREATE TABLE certificate_action (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    certificate_state_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    designation_id UUID NOT NULL,

    action certificate_action_type NOT NULL,
    comment VARCHAR(4000),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_certificate_action_certificate_state
        FOREIGN KEY (certificate_state_id)
        REFERENCES certificate_state (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_certificate_action_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_certificate_action_designation
        FOREIGN KEY (designation_id)
        REFERENCES designation (id)
        ON DELETE RESTRICT
);