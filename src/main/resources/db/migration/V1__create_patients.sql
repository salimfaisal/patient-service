CREATE TABLE patients (
                          id VARCHAR(100) PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          date_of_birth DATE NOT NULL,
                          email VARCHAR(320) NOT NULL,

                          CONSTRAINT uk_patients_email UNIQUE (email)
);