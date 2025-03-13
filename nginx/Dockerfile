FROM nginx:alpine

# Instalar dependências
RUN apk add --no-cache \
    curl \
    gettext \
    tzdata

# Criar diretório de configuração
RUN mkdir -p /etc/nginx/conf.d

# Configurar permissões
RUN chown -R nginx:nginx /etc/nginx/conf.d

# Copiar arquivos de configuração
COPY nginx.conf.template /etc/nginx/nginx.conf

# Configurar variável de ambiente padrão
ENV TZ=America/Sao_Paulo

# Expor a porta 80
EXPOSE 80

# Comando para iniciar o Nginx
CMD ["nginx", "-g", "daemon off;"] 