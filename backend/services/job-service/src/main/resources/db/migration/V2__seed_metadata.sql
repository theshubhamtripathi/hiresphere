INSERT INTO job_categories (created_at, updated_at, version, name, slug, icon, description) VALUES
 (now(), now(), 0, 'Software Engineering', 'software-engineering', 'code', 'Backend, frontend and full-stack roles'),
 (now(), now(), 0, 'Data & AI', 'data-ai', 'database', 'Data science, analytics and machine learning'),
 (now(), now(), 0, 'Design', 'design', 'palette', 'Product, UX and visual design'),
 (now(), now(), 0, 'Product Management', 'product-management', 'layers', 'Product strategy and delivery'),
 (now(), now(), 0, 'DevOps & Cloud', 'devops-cloud', 'server', 'Infrastructure, CI/CD and reliability'),
 (now(), now(), 0, 'Marketing', 'marketing', 'megaphone', 'Growth, content and performance marketing'),
 (now(), now(), 0, 'Sales', 'sales', 'trending-up', 'Inside sales, account management and BD'),
 (now(), now(), 0, 'Finance', 'finance', 'dollar-sign', 'Accounting, FP&A and financial analysis');

INSERT INTO skills (created_at, updated_at, version, name) VALUES
 (now(), now(), 0, 'Java'), (now(), now(), 0, 'Spring Boot'), (now(), now(), 0, 'Kafka'),
 (now(), now(), 0, 'PostgreSQL'), (now(), now(), 0, 'React'), (now(), now(), 0, 'TypeScript'),
 (now(), now(), 0, 'JavaScript'), (now(), now(), 0, 'Python'), (now(), now(), 0, 'Docker'),
 (now(), now(), 0, 'Kubernetes'), (now(), now(), 0, 'AWS'), (now(), now(), 0, 'SQL'),
 (now(), now(), 0, 'Machine Learning'), (now(), now(), 0, 'Node.js'), (now(), now(), 0, 'Go'),
 (now(), now(), 0, 'GraphQL'), (now(), now(), 0, 'Redis'), (now(), now(), 0, 'Figma'),
 (now(), now(), 0, 'Product Strategy'), (now(), now(), 0, 'SEO');

INSERT INTO tags (created_at, updated_at, version, name) VALUES
 (now(), now(), 0, 'Remote'), (now(), now(), 0, 'Urgent'), (now(), now(), 0, 'Featured'),
 (now(), now(), 0, 'Startup'), (now(), now(), 0, 'Enterprise'), (now(), now(), 0, 'Visa Sponsorship'),
 (now(), now(), 0, 'Equity'), (now(), now(), 0, 'Fast Growing');
