%close all;

load('reg1.csv')
load('reg2.csv')
load('reg3.csv')

load('bid1.csv')
load('bid2.csv')
load('bid3.csv')

figure;
plot(reg1);
hold on;
plot(-reg1);
hold on;
plot(bid1);
hold on;
plot(-bid1);


figure;
plot(reg2);
hold on;
plot(-reg2);
hold on;
plot(bid2);
hold on;
plot(-bid2);

figure;
plot(reg3);
hold on;
plot(-reg3);
hold on;
plot(bid3);
hold on;
plot(-bid3);