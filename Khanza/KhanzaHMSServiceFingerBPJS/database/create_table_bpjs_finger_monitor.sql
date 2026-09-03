-- Gunakan database Khanza yang sama dengan aplikasi registrasi.
CREATE TABLE IF NOT EXISTS bpjs_finger_monitor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    no_rawat VARCHAR(20) NOT NULL,
    no_kartu VARCHAR(30) NOT NULL,
    tgl_pelayanan DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_checked_at DATETIME NULL,
    next_check_at DATETIME NULL,
    response_code VARCHAR(20) NULL,
    response_message VARCHAR(255) NULL,
    requested_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bpjs_finger_monitor_no_rawat (no_rawat),
    KEY idx_bpjs_finger_monitor_due (status, next_check_at),
    KEY idx_bpjs_finger_monitor_tgl (tgl_pelayanan)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Audit setiap rencana request BPJS; circuit breaker menghitung tabel ini.
CREATE TABLE IF NOT EXISTS bpjs_finger_hit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    monitor_id BIGINT NOT NULL,
    no_rawat VARCHAR(20) NOT NULL,
    no_kartu_masked VARCHAR(30) NOT NULL,
    tgl_pelayanan DATE NOT NULL,
    attempt_no INT NOT NULL,
    requested_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    result_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTING',
    response_code VARCHAR(30) NULL,
    response_message VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_finger_hit_log_tgl (requested_at),
    KEY idx_finger_hit_log_rawat (no_rawat),
    KEY idx_finger_hit_log_monitor (monitor_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
