import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  cacheComponents: true,
  partialPrefetching: true,
  async redirects() {
    return [{ source: "/main", destination: "/", permanent: false }];
  },
  async rewrites() {
    return {
      beforeFiles: [{ source: "/", destination: "/main" }],
      afterFiles: process.env.API_ORIGIN
        ? [{ source: "/api/v1/:path*", destination: `${process.env.API_ORIGIN}/api/v1/:path*` }]
        : [],
    };
  }
};

export default nextConfig;
