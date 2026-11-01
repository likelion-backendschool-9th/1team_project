import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
  async redirects() {
    return [{ source: "/", destination: "/main", permanent: false }];
  },
  async rewrites() {
    if (!process.env.API_ORIGIN) return [];
    return [
      {
        source: "/api/v1/:path*",
        destination: `${process.env.API_ORIGIN}/api/v1/:path*`,
      },
    ];
  },
};

export default nextConfig;
