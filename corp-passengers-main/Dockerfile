FROM nginx:1.25-alpine

RUN rm -f /etc/nginx/conf.d/default.conf \
 && mkdir -p /tmp \
 && chown -R 1000:1000 /usr/share/nginx/html

COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY build/ /usr/share/nginx/html/

USER 1000:1000
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]