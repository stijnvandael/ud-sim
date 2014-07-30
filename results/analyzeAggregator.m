function [ output_args ] = analyzeAggregator( baseDir )
%ANALYZEAGGREGATOR analyze aggregator simulation

% file names
file_flex = [baseDir '/aggregator/flex.csv'];
file_bid = [baseDir '/aggregator/bid.csv'];
file_power = [baseDir '/aggregator/power.csv'];
file_reg = [baseDir '/aggregator/reg.csv'];
file_scenario = [baseDir '/aggregator/scenario.csv'];

% load files into variable
logs_schedule = [];
try
    fid = fopen(file_flex,'r');
    C = textscan(fid, repmat('%s',1,9), 'delimiter',',', 'CollectOutput',true);
    C = C{1};
    logs_schedule = [logs_schedule; C];
    fclose(fid);
catch exception
    disp(['Warning, file ' file ' not found.']);
end
bids = load(file_bid)';
chargePower = load(file_power)';
regPower = load(file_reg)';
evScenario = load(file_scenario);

% plot regPower and bids
figure;
plot(regPower);
hold on;
plot(bids, 'k');

% create aggregated flex graph
endTime = length(chargePower);
flexMin_total = zeros(1,endTime+1);
flexMax_total = zeros(1,endTime+1);
flexMinInner_total = zeros(1,endTime+1);
flexMaxInner_total = zeros(1,endTime+1);
flexPower_total = zeros(1,endTime+1); %equaly long as Flexmin/max to show steps
%for each EV...
for n=1:size(evScenario,1)
    Tarr = evScenario(n,2);
    Tdep = evScenario(n,3);
    Ecurr = evScenario(n,4);
    Ereq = evScenario(n,5);
    Ecap = evScenario(n,7);
    Pmax = evScenario(n,8);
    %create individual flex graph
    [ flexMin_inner, flexMax_inner, flexMin_outer, flexMax_outer, flexPower ] = createFlexGraph( Ecurr, Ereq, Ecap, (Tdep-Tarr), Pmax, 1 );
    %add zeros to account for arrival time
    flexMin = [zeros(1,Tarr) flexMin_outer ones(1,endTime-Tdep).*flexMin_outer(1,end)];
    flexMax = [zeros(1,Tarr) flexMax_outer  ones(1,endTime-Tdep).*flexMax_outer(1,end)];
    flexMinInner_total = [zeros(1,Tarr) flexMin_inner ones(1,endTime-Tdep).*flexMin_inner(1,end)];
    flexMaxInner_total = [zeros(1,Tarr) flexMax_inner  ones(1,endTime-Tdep).*flexMax_inner(1,end)];
    flexPower = [zeros(1,Tarr) flexPower flexPower(1,end) zeros(1,endTime-Tdep)];
    % AGGREGATE
    flexMin_total = flexMin_total + flexMin;
    flexMax_total = flexMax_total + flexMax;
    flexPower_total = flexPower_total + flexPower;
end

figure('name','Aggregator flexibility');
hold on;
title('flexgraphs');
% STATIC OUTER FLEX GRAPH
figure;
plot(flexMax_total, 'k');
hold on;
plot(flexMin_total, 'k');
hold on;
plot(flexMaxInner_total, 'k');
hold on;
plot(flexMinInner_total, 'k');
hold on;
plot(flexPower_total, 'k');
hold on;
plot(cumsum(-chargePower/3600));
% DYNAMIC FLEX GRAPHS
for i=1:5
    syncTime = str2num(logs_schedule{i,1})'/1000;
    syncEnergy = str2num(logs_schedule{i,2})-5000';
    flexMin = str2num(logs_schedule{i,3})';
    flexMax = str2num(logs_schedule{i,4})';
    flexPower = str2num(logs_schedule{i,5})';
    flexPath = str2num(logs_schedule{i,6})';
    hold on;
    plot(syncTime:900:syncTime+((length(flexMin)-1)*900), syncEnergy+flexMin);
   	hold on;
    plot(syncTime:900:syncTime+((length(flexMax)-1)*900), syncEnergy+flexMax);
    hold on;
    plot(syncTime:900:syncTime+((length(flexPath)-1)*900), syncEnergy+flexPath);
end

end

