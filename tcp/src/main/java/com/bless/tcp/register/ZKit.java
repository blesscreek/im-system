package com.bless.tcp.register;

import com.bless.common.constant.Constants;
import com.sun.org.apache.bcel.internal.Const;
import org.I0Itec.zkclient.ZkClient;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-25 10:25
 */

public class ZKit {
    private ZkClient zkClient;
    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }
    //一级目录标识节点名称/标识注册的是tcp服务，还是websocket服务/最后是ip加端口
    //im-coreRoot/tcp/ip:port
    public void createRootNode() {
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists){
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }
        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootWeb);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootWeb);
        }

    }
    //ip+port
    public void createNode(String path){
        if(!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }
}
