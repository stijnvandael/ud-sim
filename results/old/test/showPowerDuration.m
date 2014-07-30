close all;
load firsttest;

power1 = abs(power1);
power2 = abs(power2);
power3 = abs(power3);

%round to kw
figure;
power1 = floor(power1./1000);

powerDuration1 = [];
for p=max(power1):-1:0
    powerDuration1 = [powerDuration1 ones(1,sum(power1==p)).*p];
end

plot(powerDuration1)

%round to kw
power2 = floor(power2./1000);

powerDuration2 = [];
for p=max(power2):-1:0
    powerDuration2 = [powerDuration2 ones(1,sum(power2==p)).*p];
end

hold on;
plot(powerDuration2)

%round to kw
power3 = floor(power3./1000);

powerDuration3 = [];
for p=max(power3):-1:0
    powerDuration3 = [powerDuration3 ones(1,sum(power3==p)).*p];
end

hold on;
plot(powerDuration3)