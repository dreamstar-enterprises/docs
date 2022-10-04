---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## AMORT.SCHEDULE

### About

Creates an Amortisation schedule.

#### Inputs:

  - years - the number of years
  - annual_rate - the annual interest rate
  - pv - the present value of the loan
  - annual_frequency - OPTIONAL - the number of payments each year (defaults to 12, if argument is not given)

### Code

{% capture code %}
AMORT.SCHEDULE = LAMBDA(years, annual_rate, pv, [annual_frequency],
    LET(
        _years, years,
        _annual_rate, annual_rate,
        _pv, pv,
        _annual_frequency, annual_frequency,
        _freq, SWITCH(_annual_frequency, "Annual", 1, "Quarterly", 4, "Bi-Weekly", 26, 12),
        MAKEARRAY(
            _years * _freq,
            6,
            LAMBDA(row, col,
                LET(
                    _periods, _years * _freq,
                    _periodrate, _annual_rate / _freq,
                    _principal, _pv,
                    _prinpay, PPMT(_periodrate, row, _periods, _principal),
                    _cumpay, CUMPRINC(_periodrate, _periods, _principal, 1, row, 0),
                    CHOOSE(
                        col,
                        row,
                        _principal + _cumpay - _prinpay,
                        PMT(_periodrate, _periods, _principal),
                        IPMT(_periodrate, row, _periods, _principal),
                        _prinpay,
                        _principal + _cumpay
                    )
                )
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}