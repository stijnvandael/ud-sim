function [logs_flexibility, logs_schedule, logs_pop] = analyze_flex( file )

% load file
logs_schedule = [];
try
    fid = fopen(file,'r');
    C = textscan(fid, repmat('%s',1,9), 'delimiter',',', 'CollectOutput',true);
    C = C{1};
    logs_schedule = [logs_schedule; C];
    fclose(fid);
catch exception
    disp(['Warning, file ' file ' not found.']);
end




% ANALYZE SOP vars

%static vars
arrivalTime = datenum(staticvars{1});
departureTime = datenum(staticvars{2});
Emin = staticvars{3}*1000;
reqE = staticvars{4}*1000;
Pmax = staticvars{5};
Ecap = staticvars{6};
deltaT = staticvars{7};
%derive some vars
secondsToCharge = (reqE*3600)/Pmax;
pivotTime =  departureTime - secondsToCharge/86400;
pivotEnergy = 0;
if pivotTime < arrivalTime
    pivotTime = arrivalTime;
    pivotEnergy = reqE - Pmax*(departureTime - arrivalTime)*86400/3600;
end
deltaE = deltaT/3600*Pmax;

%% load flexibility log files
logs_flexibility = [];
for(d = dates')
    % convert date info to strings
    year = sprintf('%02d',d(1));
    month = sprintf('%02d',d(2));
    day = sprintf('%02d',d(3));
    hour = sprintf('%02d',d(4));
    % load file
    file = [basedir '/sop/' year '/' month '/' day '/flexibility_' hour '.log'];
    try
        fid = fopen(file,'r');
        C = textscan(fid, repmat('%s',1,10), 'delimiter',',', 'CollectOutput',true);
        C = C{1};
        logs_flexibility = [logs_flexibility; C];
        fclose(fid);
    catch exception
        disp(['Warning, file ' file ' not found.']);
    end
end
timestamps_flexibility = datenum(logs_flexibility(:,1));
%% load schedule log files
logs_schedule = [];
for(d = dates')
    % convert date info to strings
    year = sprintf('%02d',d(1));
    month = sprintf('%02d',d(2));
    day = sprintf('%02d',d(3));
    hour = sprintf('%02d',d(4));
    % load file
    file = [basedir '/sop/' year '/' month '/' day '/schedule_' hour '.log'];
    try
        fid = fopen(file,'r');
        C = textscan(fid, repmat('%s',1,9), 'delimiter',',', 'CollectOutput',true);
        C = C{1};
        logs_schedule = [logs_schedule; C];
        fclose(fid);
    catch exception
        disp(['Warning, file ' file ' not found.']);
    end
end
timestamps_schedule = datenum(logs_schedule(:,1));
%% load pop log files
logs_pop = [];
for(d = dates')
    % convert date info to strings
    year = sprintf('%02d',d(1));
    month = sprintf('%02d',d(2));
    day = sprintf('%02d',d(3));
    hour = sprintf('%02d',d(4));
    % load file
    file = [basedir '/sop/' year '/' month '/' day '/pop_' hour '.log'];
    try
        fid = fopen(file,'r');
        C = textscan(fid, repmat('%s',1,9), 'delimiter',',', 'CollectOutput',true);
        C = C{1};
        logs_pop = [logs_pop; C];
        fclose(fid);
    catch exception
        disp(['Warning, file ' file ' not found.']);
    end
end
timestamps_pop = datenum(logs_pop(:,1));

%% plot flexibility 
figure('name','EV flexibility');
hold on;
title('flexgraphs');
% STATIC OUTER FLEX GRAPH
plot([arrivalTime departureTime],[Ecap Ecap], '-xr');
hold on;
plot([arrivalTime pivotTime departureTime],[pivotEnergy pivotEnergy reqE], '-xr');
hold on;
% STATIC INNER FLEX GRAPH
plot([arrivalTime departureTime],[Ecap-deltaE Ecap-deltaE], '-xr');
hold on;
plot([arrivalTime pivotTime departureTime],[pivotEnergy+deltaE pivotEnergy+deltaE reqE+deltaE], '-xr');
hold on;
plot([arrivalTime departureTime],[pivotEnergy+Emin pivotEnergy+Emin], '-xr');
hold on;
plot([arrivalTime departureTime],[pivotEnergy+Emin+deltaE pivotEnergy+Emin+deltaE], '-xr');
hold on;
% DYNAMIC FLEX GRAPHS
for i=1:size(logs_flexibility,1)
    status = str2double(logs_flexibility{i, 3})';
    if status == 1
        % i did a timezone correction here
        syncTime = str2double(logs_flexibility{i, 4}) /86400 + datenum(1970,1,1) + 3600/86400;
        syncEnergy = str2double(logs_flexibility{i, 5});
        nIntervals = str2double(logs_flexibility{i, 6});
        timepoints = syncTime:900/86400:syncTime+nIntervals*(900/86400);
        Emin = str2num(logs_flexibility{i, 7})';
        Emax = str2num(logs_flexibility{i, 8})';
        Pmax = str2num(logs_flexibility{i, 9})';
        % syncE is soms 0 en Emin,Emax,Pmax toch ok, raar...
        if ~isempty(Emin) && syncEnergy>0
            plot(timepoints, syncEnergy + [0 Emin]);
            hold on;
            plot(timepoints, syncEnergy + [0 Emax]);
            %plot(timepoints, syncEnergy + [0 Pmax]);
            hold on;
        end
    end
end

datetick('x','HH:MM');
%legend('battery up (W)','battery down (W)','line up (W)','line down (W)','constrained up (W)','constrained down (W)');

%% plot POP
figure('name','EV flexibility');
hold on;
title('POP');
stairs(timestamps_pop,cellfun(@str2num,logs_pop(:,3)), '-x', 'Color', [0.7 0.7 0.7]);
datetick('x','HH:MM');
legend('POP (W)');

end
