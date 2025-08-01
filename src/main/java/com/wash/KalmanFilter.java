package com.wash;

import Jama.Matrix;


    public  class KalmanFilter {
        // 状态向量: [纬度, 经度, 纬度速度, 经度速度]
        private Matrix conditionMatrix;

        // 协方差矩阵
        private Matrix covMatrix;

        // 状态转移矩阵
        private Matrix F;
        // 测量矩阵
        private Matrix H;


        // 过程噪声协方差
        private Matrix Q;
        // 测量噪声协方差
        private Matrix R;

        public KalmanFilter(double initLat, double initLon) {
            // 初始化状态：位置为初始经纬度，速度为0
            double[][] xData = {{initLat}, {initLon}, {0}, {0}};
            conditionMatrix = new Matrix(xData);

            // 初始协方差 设置1000表示初始位置的不确定性较大
            // 矩阵对角线元素表示对应状态变量的方差 不确定性
            // 非对角线元素表示状态变量之间的协方差 相关性
            covMatrix = Matrix.identity(4, 4).times(1000);

            // 时间步长 (100ms)
            double dt = 100/1000;
            //
            double[][] FData = {
                    {1, 0, dt, 0},
                    {0, 1, 0, dt},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            };
            F = new Matrix(FData);

            // 测量矩阵：只观测位置
            double[][] HData = {
                    {1, 0, 0, 0},
                    {0, 1, 0, 0}
            };
            H = new Matrix(HData);

            // 过程噪声 (模拟加速/减速)
            double[][] GData = {
                    {0.5*dt*dt, 0},
                    {0, 0.5*dt*dt},
                    {dt, 0},
                    {0, dt}
            };
            Matrix G = new Matrix(GData);


            double processNoise = 8e-11;//111000*111000  经纬度加速度的方差,换算速度单位大概在 1m/s² 即0.1g
            double[][] QBaseData = {
                    {processNoise, 0},
                    {0, processNoise}
            };
            Matrix QBase = new Matrix(QBaseData);
            //外界扰动 → 加速度变化 → 速度变化 → 位置变化
            /**
             * QBase：加速度噪声
             *    ↓ 通过运动学关系传播
             * G：将加速度影响映射到位置和速度
             *    ↓ 得到完整的噪声描述
             * Q：位置和速度的联合噪声
             */
            Q = G.times(QBase).times(G.transpose());

            // 感知数据噪声 (GPS精度假设为±10米)
            double gpsNoise = 1e-7; // 约10米精度的度数(假设111km/度)
            double[][] RData = {
                    {gpsNoise, 0},
                    {0, gpsNoise}
            };
            R = new Matrix(RData);
        }

        // 预测
        public void predict() {
            //状态转移矩阵 (匀速模型)  ->  lat=1*lat + 0*lon + dt*v_lat + 0*v_lon
            //推进系统状态 x=f*x(k-1)
            conditionMatrix = F.times(conditionMatrix);

            //更新不确定性
            covMatrix = F.times(covMatrix).times(F.transpose()).plus(Q);
        }

        // 更新状态值
        public void update(double lat, double lon) {
            double[][] zData = {{lat}, {lon}};
            Matrix z = new Matrix(zData);
            Matrix y = z.minus(H.times(conditionMatrix));//观测与上一步的残差

            //感知数据协方差，观测不确定性在测量空间的投影
            Matrix S = H.times(covMatrix).times(H.transpose()).plus(R);

            //预测/测量  有多相信预测？
            Matrix K = covMatrix.times(H.transpose()).times(S.inverse());
            //修正
            conditionMatrix = conditionMatrix.plus(K.times(y));
            covMatrix = covMatrix.minus(K.times(H).times(covMatrix));
        }

        public double getLat() { return conditionMatrix.get(0, 0); }
        public double getLon() { return conditionMatrix.get(1, 0); }


    public static void main(String[] args) {
        // 模拟参数：沿40度方向以20m/s速度移动
        double startLat = 30.0;
        double startLon = 120.0;
        double speed = 0.00018; // 每秒变化度数 (约20m/s)
        double dt = 0.1; // 100ms

        // 创建卡尔曼滤波器
        KalmanFilter kf = new KalmanFilter(startLat, startLon);

        System.out.println("时间(s)\t原始纬度\t原始经度\t滤波后纬度\t滤波后经度");

        // 生成并处理100个数据点 (10秒)
        for (int i = 0; i < 100; i++) {
            // 计算真实位置
            double t = i * dt;
            double trueLat = startLat + speed * t * Math.cos(Math.toRadians(40));
            double trueLon = startLon + speed * t * Math.sin(Math.toRadians(40));

            // 添加GPS噪声 (标准差约5米)
            double gpsNoise = 5e-8;
            double measLat = trueLat + (Math.random() - 0.5) * 4 * gpsNoise;
            double measLon = trueLon + (Math.random() - 0.5) * 4 * gpsNoise;

            // 执行卡尔曼滤波步骤
            kf.predict();
            kf.update(measLat, measLon);

            // 输出结果 (格式化保留有效位数)
            System.out.printf("%.1f\t%.8f\t%.8f\t%.8f\t%.8f%n",
                    t,
                    measLat,
                    measLon,
                    kf.getLat(),
                    kf.getLon());
        }
    }


}
