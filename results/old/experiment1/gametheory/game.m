% options.MaxFunEvals = 3000;
% 
% x0 = [0; 0; 0; 0; 0; 0];       % Make a starting guess at solution
% Aeq = [1 1 1 0 0 0;
%        0 0 0 1 1 1];
% beq = [1; 1];
% [x,fval] = fminimax(@myfun,x0,...
%                     [],[],Aeq,beq,[0;0;0;0;0;0],[1;1;1;1;1;1],[],options);


%select cplex solver
cplex = sdpsettings('verbose',1, 'showprogress', 1, 'debug', 1);
x = sdpvar(6, 1, 'full');

constraints = [];
constraints = constraints + [0 <= x <= 1];
constraints = constraints + [sum(x(1:3)) == 1];
constraints = constraints + [sum(x(4:6)) == 1];

%min
y = sdpvar(3, 1, 'full');
constraints = constraints + [y(1) == 0*x(1)-1*x(2)-2*x(3)];
constraints = constraints + [y(2) == 4*x(1)+3*x(2)+2*x(3)];
constraints = constraints + [y(3) == 8*x(1)+7*x(2)+6*x(3)];

constraints = constraints + [z <= x(4)*y(1) + x(5)*y(2) + x(6)*y(3)];

solvesdp(constraints, -z, cplex);
double(x)