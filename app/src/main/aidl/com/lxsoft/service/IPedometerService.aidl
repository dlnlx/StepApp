// IPedometerService.aidl
package com.lxsoft.service;
import com.lxsoft.bean.PedometerChartBean;
// Declare any non-default types here with import statements

interface IPedometerService {
    //开始记步
    void startCount();
    //结束记步
    void stopCount();
    //重置计步数
    void resetCount();
    //获取计步数
    int getSetpsCount();
    //获取消耗的能量
    double getCalorie();
    //获取距离
    double getDistance();
    //保存数据
    void saveData();
    //设置传感器灵敏度
    void setSensitivity(double sensitivity);
    //获取传感器灵敏度
    double getSensitivity();
    //设置采样时间间隔
    void setInterval(int interval);
    //获取采样时间
    int getInterval();
    //获取开始时间戳
    long getStartTimeStamp();
    //获取服务运行状态
    int getServiceStatus();

    //获取运动图表数据
    PedometerChartBean getCharData();
}
