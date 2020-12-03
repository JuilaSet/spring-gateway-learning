import * as Consul from "consul";
import * as koa from 'koa';
import * as Router from 'koa-router';

const PORT = 7001

const app = new koa()
const router = new Router()

// 初始化健康检查
router.get("/health", (ctx)=>{
    ctx.body = { ok: true }
})

router.get("/info", (ctx)=> {
    ctx.body = `node-server:${PORT}`
})

// 初始化 Consul 客户端
const consul = new Consul({
    // 配置 Consul 端口和地址
    host: "127.0.0.1",
    port: "8500",
    promisify: true, // 启动 Promise 风格，默认为 Callback
});

// 服务注册与健康检查
consul.agent.service.register(
    {
        id: 'node-fun-provider-7001', //  服务注册标识
        name: 'fun-provider', // 注册的服务名称
        address: 'localhost', //需要注册的服务地址 & 端口
        port: PORT,
        check: {
            // 健康检查路径, interval 参数为必须设置
            http: 'http://localhost:7001/health',
            interval: '10s',
            ttl: '5s',
        },
    },
    (err, data, result) => {
        if (err) {
            console.error(err);
            throw err;
        }
        console.log("node-fun-provider 注册成功！");
    }
);

// 取消注册
consul.agent.service.deregister('node-fun-provider7001')

app.use(router.routes()).use(router.allowedMethods())
app.listen(7001, ()=>{
    console.log(`server listen on ${PORT}`);
})
