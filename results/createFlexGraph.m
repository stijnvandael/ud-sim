function [ flexMin_inner, flexMax_inner, flexMin_outer, flexMax_outer, flexPower ] = createFlexGraph( Ecurr, Ereq, Ecap, Tdep, Pmax, optimizationInterval )

    Nopt = 3600/optimizationInterval;

    flexMax_outer = zeros(1,Tdep+1);
    for t=1:Tdep
        flexMax_outer(1,t+1) = min(flexMax_outer(1,t) + Pmax/Nopt, Ecap - Ecurr);
    end

    flexMin_outer = [zeros(1,Tdep) Ereq - Ecurr];
    for t=Tdep:-1:1
        flexMin_outer(1,t) = max(flexMin_outer(1,t+1) - Pmax/Nopt, 0);
    end
    
    flexMax_inner = zeros(1,Tdep+1);
    for t=1:Tdep
        flexMax_inner(1,t+1) = min(flexMax_inner(1,t) + Pmax/Nopt, Ecap - Ecurr - Pmax/4);
    end
    
    flexMin_inner = [zeros(1,Tdep) Ereq - Ecurr + Pmax/4];
    for t=Tdep:-1:1
        flexMin_inner(1,t) = max(flexMin_inner(1,t+1) - Pmax/Nopt, 0);
    end
    
    flexPower = ones(1,Tdep).*Pmax;
    
end

