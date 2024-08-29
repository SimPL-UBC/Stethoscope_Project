# statistical_analysis.m

## Load Data
```{warning}
For this script, don't load the waveform, this script expect the time stamp and bpm value of each 
audio you pass in, you can get it from either the heartbeat_analysis.m or the HRV.m and then save them
as .mat file
```
```matlab
clear; clc; close all
load('sample_HB.mat');
HB = {bpm_values_60HB bpm_values_after_run bpm_values_sleep_1 bpm_values_sleep_2};
time_stamps = {time_stamps_60HB time_stamps_after_run time_stamps_sleep_1 time_stamps_sleep_2};
name = {'60HB', 'after_run', 'sleep_1', 'sleep_2'};
```

## Shapiro-Wilk Test (Test for Normality)
```matlab
for i = 1:length(HB)
    % Shapiro-Wilk test - determine whether our data is normal or not
    [h, p, W] = swtest(HB{i}, 0.05);
    if h == 1
    fprintf('%s is not normally distributed (p-value = %.4f).\n', name{i}, p);
    else
        fprintf('%s is normally distributed (p-value = %.4f).\n', name{i}, p);
    end
end
```
Shapiro-Wilk test is a statistical test to check if your data follows a normal distribution or not.
The important thing is the p-value.
If the p-value is below a specified threshold (usually 0.05), the test rejects the null hypothesis, 
which means your data is not normally distributed. 

## Wilcoxon Signed-sum Test (Test for Mean Difference for Independent Sample)
```matlab
% Wilcoxon signed-sum test for 60HB vs after_run, assume they are independent
[p_non_sleep, h_non_sleep, stats_non_sleep] = ranksum(HB{1}, HB{2});
fprintf('non_sleep comparison test:\n');
fprintf('P-value: %f\n', p_non_sleep);
fprintf('Hypothesis test result (h): %d\n', h_non_sleep);
disp(stats_non_sleep);
```
Wilcoxon signed-rank test is a non-parametric (doesn't need sample to be normally distributed)
statistical test used to compare two independent samples to check if their population mean ranks differ (is it statistically significant or not).

## Wilcoxon Rank-Sum Test (Test for Mean Difference for Dependent Sample)
```matlab
% Wilcoxon signed-rank for sleep_1 vs sleep_2 (they are dependent, same recording at different time)
if length(HB{3}) >= length(HB{4})
    [p_sleep, h_sleep, stats_sleep] = signrank(HB{3}(1:length(HB{4})), HB{4});
else
    [p_sleep, h_sleep, stats_sleep] = signrank(HB{3}, HB{4}(1:length(HB{3})));
end
fprintf('sleep comparison test:\n');
fprintf('P-value: %f\n', p_sleep);
fprintf('Hypothesis test result (h): %d\n', h_sleep);
disp(stats_sleep);
```
The Wilcoxon rank-sum test is a non-parametric (which means we can use it without assuming normal distribution) test used to compare two 
dependent samples to assess whether their population mean ranks differ. 
One thing to keep note is that the data must have the **same length**, that's the reason for the if block above.

## Curve Fitting 

```matlab
guess = [60 100 -0.01];
nan_idx = isnan(bpm_values_after_run);
mean_value = nanmean(bpm_values_after_run, 'all');
bpm_values_after_run(nan_idx) = mean_value;
fitting_function = fittype(@(a, b, lambda, x) a*exp(lambda*x) + b);
[fitted_curve,gof] = fit(time_stamps_after_run(1:500),bpm_values_after_run(1:500),fitting_function,StartPoint=guess);
```
Curve fitting toolbox to get the decay of after running recording. One thing you might notice is that it takes the first 500 points.
That number is completely arbitrary, so you change it however you want. Rest of the code is plotting

## Full Code
```matlab
clear; clc; close all
load('sample_HB.mat');
HB = {bpm_values_60HB bpm_values_after_run bpm_values_sleep_1 bpm_values_sleep_2};
time_stamps = {time_stamps_60HB time_stamps_after_run time_stamps_sleep_1 time_stamps_sleep_2};
name = {'60HB', 'after_run', 'sleep_1', 'sleep_2'};
for i = 1:length(HB)
    % Shapiro-Wilk test - determine whether our data is normal or not
    [h, p, W] = swtest(HB{i}, 0.05);
    if h == 1
    fprintf('%s is not normally distributed (p-value = %.4f).\n', name{i}, p);
    else
        fprintf('%s is normally distributed (p-value = %.4f).\n', name{i}, p);
    end
end
%sleep 2 is normally distributed, might be an error in my case
%% Comparison test
% Data isn't ideal for comparison test, but this is just a proof of concept
% that the code works

% Wilcoxon signed-sum test for 60HB vs after_run, assume they are
% independent
[p_non_sleep, h_non_sleep, stats_non_sleep] = ranksum(HB{1}, HB{2});
fprintf('non_sleep comparison test:\n');
fprintf('P-value: %f\n', p_non_sleep);
fprintf('Hypothesis test result (h): %d\n', h_non_sleep);
disp(stats_non_sleep);
% h = 1, we reject the hypothesis that they are the same

% Wilcoxon signed-rank for sleep_1 vs sleep_2 (they are dependent, same recording at different time)
% should be valid, but sleep 2 is normal by shapiro wilk, assume it's not
% normal if I have more recording
if length(HB{3}) >= length(HB{4})
    [p_sleep, h_sleep, stats_sleep] = signrank(HB{3}(1:length(HB{4})), HB{4});
else
    [p_sleep, h_sleep, stats_sleep] = signrank(HB{3}, HB{4}(1:length(HB{3})));
end
fprintf('sleep comparison test:\n');
fprintf('P-value: %f\n', p_sleep);
fprintf('Hypothesis test result (h): %d\n', h_sleep);
disp(stats_sleep);
% Null hypothesis was rejected
% Once I have more than one sample of data, say 5 each of running_sample,
% sleep and resting, I'll do a kruskal test

%% Curve fit after_run
guess = [60 100 -0.01];

nan_idx = isnan(bpm_values_after_run);
mean_value = nanmean(bpm_values_after_run, 'all');
bpm_values_after_run(nan_idx) = mean_value;

fitting_function = fittype(@(a, b, lambda, x) a*exp(lambda*x) + b);
[fitted_curve,gof] = fit(time_stamps_after_run(1:500),bpm_values_after_run(1:500),fitting_function,StartPoint=guess);

plot(time_stamps_after_run(1:500), bpm_values_after_run(1:500))
hold on

fitted_bpm = fitted_curve.a * exp(fitted_curve.lambda * time_stamps_after_run) + fitted_curve.b;
plot(time_stamps_after_run(1:500), fitted_bpm(1:500))
xlabel('Time (s)')
ylabel('BPM')
title('Curve fitting analysis chopped up')

fprintf('Fit parameter:\n')
disp(fitted_curve)
fprintf('\nanalysis\n')
disp(gof)
hold off

% plot full curve
figure;
plot(time_stamps_after_run, bpm_values_after_run)
hold on
plot(time_stamps_after_run, fitted_bpm)
xlabel('Time (s)')
ylabel('BPM')
title('Curve fitting analysis chopped up')
```
