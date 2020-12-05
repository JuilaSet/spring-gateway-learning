import * as koa from 'koa';
import * as Router from 'koa-router';

const PORT = 8889

const app = new koa()
const router = new Router()

// 初始化健康检查
router.get("/health", (ctx)=>{
    ctx.body = { ok: true }
})

router.get("/info", (ctx)=> {
    ctx.body = `node-server:${PORT}`
})

app.use(router.routes()).use(router.allowedMethods())
app.listen(7001, ()=>{
    console.log(`server listen on ${PORT}`);
})
