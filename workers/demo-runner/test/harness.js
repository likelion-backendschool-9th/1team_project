// 테스트 전용 진입점. 같은 로컬 R2에 데이터를 넣고 지울 수 있게 __seed 경로만 앞에 붙인다. 배포본(src/index.js)에는 없다.
import worker from "../src/index.js";

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    if (url.pathname.startsWith("/__seed/")) {
      const key = decodeURIComponent(url.pathname.slice("/__seed/".length));
      if (request.method === "PUT") await env.DEMO_BUCKET.put(key, await request.text());
      if (request.method === "DELETE") await env.DEMO_BUCKET.delete(key);
      if (request.method === "GET") {
        const listed = await env.DEMO_BUCKET.list({ prefix: key });
        return Response.json(listed.objects.map((object) => object.key));
      }
      return new Response("ok");
    }
    return worker.fetch(request, env, ctx);
  },
};
