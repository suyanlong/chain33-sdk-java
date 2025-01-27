package cn.chain33.javasdk.client;

import cn.chain33.javasdk.model.protobuf.CommonProtobuf;
import cn.chain33.javasdk.utils.ConfigUtil;
import io.grpc.EquivalentAddressGroup;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GrpcClientPerfTest {

    private static int totalSize;

    // 统计
    public static synchronized void setSize(int size) {
        totalSize += size;
    }

    public static void main(String[] args) {
        int t = 4;
        int sleep = 5;
        String ip = "multiple";
        List<EquivalentAddressGroup> addresses = ConfigUtil.getNodes("node.properties");
        System.out.println("list:" + addresses);
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                t = Integer.parseInt(args[i]);
            }
            if (i == 1) {
                sleep = Integer.parseInt(args[i]);
            }
            if (i == 2) {
                ip = args[i];
            }
            System.out.println(args[i]);
        }

        try {
            CountDownLatch countDownLatch = new CountDownLatch(t);
            for (int i = 0; i < t; i++) {
                Thread thread = new Thread(new WorkJob(countDownLatch, sleep, ip, addresses));
                thread.setName("线程-" + (i + 1));
                thread.start();
                countDownLatch.countDown();
            }
        } finally {
            System.out.println("send total num " + totalSize);
        }
    }

    static class WorkJob implements Runnable {

        // 区块链节点IP
        String mainIp = "127.0.0.1";
        // 平行链服务端口
        int mainPort = 8801;
        int grpcMainPort = 8802;
        RpcClient clientMain;

        // 上链存证的内容(电子档案上链)
        String content = "{\"档案编号\":\"ID0000001\",\"企业代码\":\"QY0000001\",\"业务标识\":\"DA000001\",\"来源系统\":\"OA\", \"文档摘要\",\"0x93689a705ac0bb4612824883060d73d02534f8ba758f5ca21a343beab2bf7b47\"}";

        GrpcClient javaGrpcClient;

        private final CountDownLatch countDownLatch;

        private final int sleep;

        public WorkJob(CountDownLatch countDownLatch, int sleep, String ip,
                List<EquivalentAddressGroup> socketAddress) {
            this.countDownLatch = countDownLatch;
            this.sleep = sleep;
            this.javaGrpcClient = new GrpcClient(ip, socketAddress);
            this.clientMain = new RpcClient(ip, mainPort);
        }

        @Override
        public void run() {
            int count = 0;
            long start = System.currentTimeMillis();
            while (true) {
                try {
                    /**
                     *
                     */
                    Thread.sleep(sleep * 1000);
                    // 存证智能合约的名称（简单存证，固定就用这个名称）
                    String execer = "user.write";
                    // jsonrpc
                    // String contractAddress = clientMain.convertExectoAddr(execer);
                    // 获取签名用的私钥
                    Account account = new Account();
                    String privateKey = account.newAccountLocal().getPrivateKey();
                    long txHeight = javaGrpcClient.run(o -> o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build()))
                            .getHeight();
                    System.out.println("txheight:" + txHeight);
                    long txHeight2 = javaGrpcClient
                            .run(o -> o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build())).getHeight();
                    System.out.println("txheight:" + txHeight2);
                    long txHeight3 = javaGrpcClient
                            .run(o -> o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build())).getHeight();
                    System.out.println("txheight:" + txHeight3);
                    long txHeight4 = javaGrpcClient
                            .run(o -> o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build())).getHeight();
                    System.out.println("txheight:" + txHeight4);
                    // TransactionAllProtobuf.Transaction transaction = TransactionUtil.createTransferTx2(privateKey,
                    // contractAddress, execer, content.getBytes(),
                    // TransactionUtil.DEFAULT_FEE, txHeight);
                    // try {
                    // CommonProtobuf.Reply result = javaGrpcClient.run(o->o.sendTransaction(transaction));
                    // System.out.println("result:"+result.getIsOk()+" hash:"+
                    // HexUtil.toHexString(result.getMsg().toByteArray()));
                    // } catch (StatusRuntimeException e) {
                    // //e.printStackTrace();
                    // System.out.println(Thread.currentThread().getName()+"send method err" +
                    // e.getMessage()+"-height"+txHeight);
                    // long txHeight2 =
                    // javaGrpcClient.run(o->o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build())).getHeight();
                    // System.out.println(Thread.currentThread().getName()+"send method err" +
                    // e.getMessage()+"-height"+txHeight2);
                    // long txHeight3 =
                    // javaGrpcClient.run(o->o.getLastHeader(CommonProtobuf.ReqNil.newBuilder().build())).getHeight();
                    // System.out.println(Thread.currentThread().getName()+"send method err" +
                    // e.getMessage()+"-height"+txHeight3);
                    // //javaGrpcClient.shutdown();
                    // //countDownLatch.await();
                    // }
                    count++;
                    setSize(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("send err" + e.getMessage());
                } finally {
                    long end = System.currentTimeMillis();
                    System.out.println(
                            Thread.currentThread().getName() + "-发送交易总数" + count + "-cost" + (start - end) + "ms");
                    System.out.println("send total num " + totalSize);
                }
            }

        }

    }
}
