# stage 1 is to build the react app : 

   FROM  node:20-alpine AS builder

   WORKDIR /app

   COPY package*.json .

   RUN npm ci

   # always use ci in docker it is more deterministic and faster

   COPY . . 

   # copy all the source code 

    RUN npm run build



    # stage 2 : Server with Nginx : 

    FROM nginx:alpine AS runtime
    
    # nginx is a super fast web server

    COPY --from=builder /app/dist usr/share/nginx/html

    # copy the built react files into nginx' serve directory

    COPY nginx.conf /etc/nginx/conf.d/default.conf

    # custom nginx config file that we created

    EXPOSE 80

    CMD ["nginx", "-g", "daemon off;"]