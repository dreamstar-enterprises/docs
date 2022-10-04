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

## FIRST.MONDAY

### About

Returns the first Monday of the month.

### Code

{% capture code %}
FIRST.MONDAY = LAMBDA(anydate,
    LET(
        start_of_month, DATE(YEAR(anydate), MONTH(anydate), 1),
        CHOOSE(WEEKDAY(start_of_month), 1, 0, 6, 5, 4, 3, 2) + start_of_month
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}