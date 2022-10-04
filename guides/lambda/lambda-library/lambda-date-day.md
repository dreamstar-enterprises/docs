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

## DATE.DAY

### About

Returns the Day of a Date.

### Code

{% capture code %}
DATE.DAY = LAMBDA(anydate,
    LET(
        number_array, {1, 2, 3, 4, 5, 6, 7},
        day_array, {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"},
        XLOOKUP(WEEKDAY(anydate, 1), number_array, day_array)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}