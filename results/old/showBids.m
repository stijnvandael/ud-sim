close all;

load('bid1.csv')
load('bid2.csv')
load('bid3.csv')


bar([sum(bid1)/3600 sum(bid2)/3600 sum(bid2)/3600])