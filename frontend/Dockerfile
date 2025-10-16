# =================================================================
# STAGE 1: Build Angular app (Node.js environment)
# =================================================================
# Alpine bcs lightweight
FROM node:18-alpine AS builder 

WORKDIR /app

# Firefox for testing
RUN apk add --no-cache firefox 

COPY package*.json ./

RUN npm ci

COPY . .

# Env for firefox
ENV FIREFOX_BIN=/usr/bin/firefox 

RUN npx ng test --browsers=FirefoxHeadless --watch=false

RUN npm run build

# =================================================================
# STAGE 2: Serve static files (Nginx)
# =================================================================
FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]