CREATE USER 'ide_user'@'localhost' IDENTIFIED BY 'OIS';
GRANT ALL PRIVILEGES ON ide_collaboratif.* TO 'ide_user'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON ide_collaboratif.* TO 'ide_user'@'localhost';
FLUSH PRIVILEGES;
