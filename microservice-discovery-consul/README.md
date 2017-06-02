#先安装和配置consul,后启动项目

###Consul 简化了分布式环境中的服务的注册和发现流程，通过 HTTP 或者 DNS 接口发现。支持外部 SaaS 提供者等。

####consul提供的一些关键特性：

    service discovery：consul通过DNS或者HTTP接口使服务注册和服务发现变的很容易，一些外部服务，例如saas提供的也可以一样注册。
    health checking：健康检测使consul可以快速的告警在集群中的操作。和服务发现的集成，可以防止服务转发到故障的服务上面。
    key/value storage：一个用来存储动态配置的系统。提供简单的HTTP接口，可以在任何地方操作。
    multi-datacenter：无需复杂的配置，即可支持任意数量的区域(数据中心)。
    官方网站：https://www.consul.io/

    到官网下载对应系统的版本
        下载解压，里面只有一个consul可执行文件，执行
    [root@localhost consul-0.6.4]# consul
        usage: consul [--version] [--help] <command> [<args>]

    Available commands are:
        agent          Runs a Consul agent
        configtest     Validate config file
        event          Fire a new event
        exec           Executes a command on Consul nodes
        force-leave    Forces a member of the cluster to enter the "left" state
        info           Provides debugging information for operators
        join           Tell Consul agent to join cluster
        keygen         Generates a new encryption key
        keyring        Manages gossip layer encryption keys
        leave          Gracefully leaves the Consul cluster and shuts down
        lock           Execute a command holding a lock
        maint          Controls node or service maintenance mode
        members        Lists the members of a Consul cluster
        monitor        Stream logs from a Consul agent
        reload         Triggers the agent to reload configuration files
        rtt            Estimates network round trip time between nodes
        version        Prints the Consul version
        watch          Watch for changes in Consul

    其中，最常用到的命令是agent
        输入consul agent -h 可以查看帮助。其中常见的参数解释如下：
            -advertise：     通知展现地址用来改变我们给集群中的其他节点展现的地址，一般情况下-bind地址就是展现地址
            -bootstrap：     用来控制一个server是否在bootstrap模式，在一个datacenter中只能有一个server处于bootstrap模式，当一个server处于bootstrap模式时，可以自己选举为raft leader。
            -bootstrap-expect：在一个datacenter中期望提供的server节点数目，当该值提供的时候，consul一直等到达到指定sever数目的时候才会引导整个集群，该标记不能和bootstrap公用
            -bind：          该地址用来在集群内部的通讯，集群内的所有节点到地址都必须是可达的，默认是0.0.0.0
            -client：        consul绑定在哪个client地址上，这个地址提供HTTP、DNS、RPC等服务，默认是127.0.0.1
            -config-file：   明确的指定要加载哪个配置文件
            -config-dir：    配置文件目录，里面所有以.json结尾的文件都会被加载
            -data-dir：      提供一个目录用来存放agent的状态，所有的agent允许都需要该目录，该目录必须是稳定的，系统重启后都继续存在
            -dc：            该标记控制agent允许的datacenter的名称，默认是dc1
            -encrypt：       指定secret key，使consul在通讯时进行加密，key可以通过consul keygen生成，同一个集群中的节点必须使用相同的key
            -join：          加入一个已经启动的agent的ip地址，可以多次指定多个agent的地址。如果consul不能加入任何指定的地址中，则agent会启动失败，默认agent启动时不会加入任何节点。
            -retry-join：    和join类似，但是允许你在第一次失败后进行尝试。
            -retry-interval：两次join之间的时间间隔，默认是30s
            -retry-max：     尝试重复join的次数，默认是0，也就是无限次尝试
            -log-level：     consul agent启动后显示的日志信息级别。默认是info，可选：trace、debug、info、warn、err。
            -node：          节点在集群中的名称，在一个集群中必须是唯一的，默认是该节点的主机名
            -protocol：      consul使用的协议版本
            -rejoin：        使consul忽略先前的离开，在再次启动后仍旧尝试加入集群中。
            -server：        定义agent运行在server模式，每个集群至少有一个server，建议每个集群的server不要超过5个
            -syslog：        开启系统日志功能，只在linux/osx上生效
            -ui-dir:        提供存放web ui资源的路径，该目录必须是可读的
            -pid-file:      提供一个路径来存放pid文件，可以使用该文件进行SIGINT/SIGHUP(关闭/更新)agent


####要想利用consul提供的服务实现服务的注册与发现，我们需要建立consul cluster。
####在consul方案中，每个提供服务的节点上都要部署和运行consul的agent，所有运行consul agent节点的集合构成consul cluster。
####consul agent有两种运行模式：server和client。这里的server和client只是consul集群层面的区分，与搭建在cluster之上的应用服务无关。
####以server模式运行的consul agent节点用于维护consul集群的状态，
####官方建议每个consul cluster至少有3个或以上的运行在server mode的agent，client节点不限。

###我们这里以安装三个节点为例，环境配置如下

    192.168.1.100 以server模式运行
    192.168.1.101，192.168.1.102 以client模式运行

    一：配置consul
    把上面下载的文件解压，并把consul拷贝到/opt/consul目录，然后把/opt/consul目录加入到环境变量（三个节点依次配置）

    二：运行
    1：在192.168.1.100节点上面进行
    cd /opt/consul
    mkdir data
    consul agent -server -bootstrap -bind=0.0.0.0 -client=192.168.1.100 -data-dir=data -ui -node=192.168.1.100
    这样，就启动了一个节点
    2：在192.168.1.101节点上面进行
    cd /opt/consul
    mkdir data
    consul agent -bind=0.0.0.0 -client=192.168.1.101 -data-dir=data -node=192.168.1.101 -join=192.168.1.100
    3：在192.168.1.102节点上面进行
    cd /opt/consul
    mkdir data
    consul agent -bind=0.0.0.0 -client=192.168.1.102 -data-dir=data -node=192.168.1.102 -join=192.168.1.100
    全部节点启动完之后
    访问http://192.168.1.100:8500/  即可查看consul集群的管理页面

    在任意节点执行consul members 即可查看集群节点信息
    这里在192.168.1.100节点执行
    [root@localhost consul-0.6.4]# consul members -rpc-addr=192.168.1.100:8400
    Node           Address             Status  Type    Build  Protocol  DC
    192.168.1.101  192.168.1.101:8301  alive   client  0.6.4  2         dc1
    192.168.1.102  192.168.1.102:8301  alive   client  0.6.4  2         dc1
    192.168.1.100  192.168.1.100:8301  alive   server  0.6.4  2         dc1

    关闭节点
    consul leave -rpc-addr=192.168.1.100:8400