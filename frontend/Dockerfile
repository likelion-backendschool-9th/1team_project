# 1단계: 의존성 설치
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# 개발용 스테이지
FROM node:22-alpine AS dev
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
EXPOSE 3000
CMD ["npm", "run", "dev"]

# 2단계: 빌드
FROM node:22-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
ENV NEXT_TELEMETRY_DISABLED=1
ARG API_PROXY_TARGET=http://backend:8080
ENV API_ORIGIN=$API_PROXY_TARGET
# DM 실시간 주소. NEXT_PUBLIC_ 값은 빌드할 때 코드에 들어간다.
# /api/v1/ws처럼 상대 주소면 접속한 주소 기준으로 ws://, wss://를 붙인다(Nginx가 백엔드로 넘긴다).
ARG NEXT_PUBLIC_WS_URL=/api/v1/ws
ENV NEXT_PUBLIC_WS_URL=$NEXT_PUBLIC_WS_URL
RUN npm run build

# 3단계: 실행
FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV HOSTNAME=0.0.0.0
ENV PORT=3000
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
