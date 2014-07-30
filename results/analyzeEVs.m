function [ output_args ] = analyzeEVs( baseDir )
%ANALYZEAGGREGATOR analyze aggregator simulation

% file names
file_power = [baseDir '/1/power.csv'];
file_soc = [baseDir '/1/soc.csv'];

% load files into variable
energy = load(file_soc)';
chargePower = load(file_power)';

figure('name','EV flexibility');
hold on;
title('flexgraph');
plot(energy);
plot(cumsum(-chargePower)/3600);
end

