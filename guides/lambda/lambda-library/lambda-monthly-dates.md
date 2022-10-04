---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Data & Time functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## MONTHLY.DATES

### About

Creates a series of dates, e.g. for a payment or receipt schedule.

#### Inputs:

  - start_date - the starting date of the payment term (typically the date the first payment is due)
  - term_years - the number of years over which the payment must be made
  - period_months - the number of months between each payment
  - start_or_end - return start date of months ["start"], or end date of months ["end"]
  - endpoint_offset - OPTIONAL - the number of periods to include before the first payment date and after the last payment date


### Code

{% capture code %}
MONTHLY.DATES = LAMBDA(start_date, term_years, period_months, [start_or_end], [endpoint_offset],
    LET(
        _rnd, LAMBDA(val, then, IF(NOT(ISNUMBER(val)), then, ROUND(val, 0))),
        _is_text, LAMBDA(val, then, IF(NOT(ISTEXT(val)), then, val)),
        _sd, _rnd(start_date, NA()),
        _t, _rnd(term_years, NA()),
        _eo, IF(ISOMITTED(endpoint_offset), 1, _rnd(endpoint_offset, 1)),
        _es, IF(ISOMITTED(start_or_end), "start", _is_text(start_or_end, "not text")),
        _pm, _rnd(period_months, 3),
        _osd, EDATE(_sd, -(_pm * _eo)),
        _ppy, 12 / _pm,
        _s, SWITCH(
            _es,
            "start",
            DATE(YEAR(_osd), SEQUENCE(_t * _ppy + _eo * 2 + 1, 1, MONTH(_osd), _pm), 1),
            "end",
            DATE(YEAR(_osd), SEQUENCE(_t * _ppy + _eo * 2 + 1, 1, MONTH(_osd) + 1, _pm), 0),
            "not text",
            "ERROR: start_or_end argument is not of type 'text'",
            "ERROR: start_or_end argument should have the value 'start' or 'end'"
        ),
        _s
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}