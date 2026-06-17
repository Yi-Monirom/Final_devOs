#!/bin/bash
set -e

docker exec web_choice_a bash -c "
rm -f /etc/nginx/sites-enabled/default
cat > /etc/nginx/sites-available/spring-boot << 'EOF'
server {
    listen 80;
    server_name _;
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF
ln -sf /etc/nginx/sites-available/spring-boot /etc/nginx/sites-enabled/
nginx -t && nginx -s reload
"

docker exec web_choice_a bash -c "mkdir -p /var/www/html/app/demo/backups"

docker exec -e JAVA_HOME=/opt/java/openjdk web_choice_a bash -c "cd /var/www/html/app/demo && mvn clean package -DskipTests"

docker exec web_choice_a bash -c "
cd /var/www/html/app/demo && mvn test -Dspring.profiles.active=test
"

docker exec web_choice_a bash -c 'mysqldump -h mysql -u root -pHello@123 student_employee > /var/www/html/app/demo/backups/backup-$(date +%Y%m%d-%H%M%S).sql'
