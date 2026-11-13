-- Seed inicial de usuarios administradores (equivalentes al legacy: luis y esteban, clave demo: 1234)
INSERT INTO usuarios (email, password_hash, rol, cliente_id)
VALUES
    ('luis@autoclean.com', '$2a$10$4yyk9iF.lQA2IgssM8lHPebXkyKz.zm1DkzAtbbdCWNJ8FDqIFu8.', 'ADMIN', NULL),
    ('esteban@autoclean.com', '$2a$10$n3/1bPyk77fG.T2YvW7Iy.LtCWgSiMDfJjm66LjIp0afeRPxxROfG', 'ADMIN', NULL)
ON CONFLICT (email) DO NOTHING;

