-- Insert sample cloud resources data
INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Production Web Server', 'VM', 'ACTIVE', 'Main production web server hosting the API', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Database Server', 'Database', 'ACTIVE', 'Primary MySQL database for production', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Backup Storage', 'Storage', 'ACTIVE', 'S3 backup storage for all configurations and data', 'us-west-2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Development VM', 'VM', 'INACTIVE', 'Development environment for testing new features', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Cache Server', 'Database', 'ACTIVE', 'Redis cache for performance optimization', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Log Storage', 'Storage', 'ACTIVE', 'Centralized logging and monitoring storage', 'eu-west-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Load Balancer', 'Network', 'ACTIVE', 'Application load balancer for traffic distribution', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Staging Database', 'Database', 'ACTIVE', 'Staging environment database for testing', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('CDN Distribution', 'Network', 'ACTIVE', 'CloudFront CDN for content delivery', 'us-east-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cloud_resources (name, type, status, description, region, created_at, updated_at) VALUES 
('Legacy System', 'VM', 'TERMINATED', 'Old legacy system no longer in use', 'ap-south-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
