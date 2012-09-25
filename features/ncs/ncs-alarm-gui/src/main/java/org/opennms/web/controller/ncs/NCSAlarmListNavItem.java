/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.ncs;

import javax.servlet.http.HttpServletRequest;

import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.PageNavEntry;

public class NCSAlarmListNavItem implements PageNavEntry {
    
    private String m_name;
    private String m_url;
    
    public String getName() {
        return m_name;
    }

    public String getUrl() {
        return m_url;
    }

    public DisplayStatus evaluate(HttpServletRequest request) {
        return null;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void setUrl(String url) {
        m_url = url;
    }

}
