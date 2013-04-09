# --------------------------------------------------------------------
# GiD to IMPACT Interface is GUI problemtype module for Impact
# Copyright (C) 2004-2013  Impact Developer Team
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>. 
#
# --------------------------------------------------------------------
#   
# File name:        Impact.tcl
# Author:           Waluyo Adi siswanto
# Date:             20 Feb 2011
#   
# --------------------------------------------------------------------
# Interface Last modified by: Waluyo Adi Siswanto
# Date:             -- April 2013 (written as the version)
# --------------------------------------------------------------------
#

proc InitGIDProject {dir} {

      set materials  [.central.s info materials] 
      set conditions [.central.s info conditions ovpnt] 

      CreateWindow $dir $materials $conditions
}


proc CreateWindow {dir mat cond} {
    
    set w .gid.win_impact

    InitWindow $w "GiD to Impact Interface" Impact

    frame $w.top
    label $w.top.title_text -text "      Impact: Dynamic Finite Element Program Suite      "   
 
    frame $w.information  -relief ridge -bd 5
    label $w.information.path        -text " "
    label $w.information.materials   -text "             Version --/Apr/2013\n \ \ \ \ \ \ \ \ \ \ See menu Impact-Help for detail explanation"
    label $w.information.conditions  -text "\n --> bug report: http://sourceforge.net/p/impact/tickets <--\n \n"                                              
#    label $w.information.conditions  -text "      --> or email: was.uthm@gmail.com <--\n"

    frame $w.bottom
    button $w.bottom.start -text "CONTINUE" \
           -height 1 -width 14 -command "destroy $w"
 
    pack $w.top.title_text -pady 10
    pack $w.information.path $w.information.materials \
         $w.information.conditions -side top -anchor w
    
    pack $w.bottom.start  -side left -anchor center
    pack $w.top 
    pack $w.information -expand yes -fill both
    pack $w.bottom -side top -padx 6 -pady 10 -ipady 2
    
    UpdateMenusImpact
} 


proc UpdateMenusImpact { } {
     GiDMenu::Create Impact-Help PREPOST -1 =
     GiDMenu::InsertOption   Impact-Help  [list   "About GiD to Impact"]  0 PREPOST \
             {GiDCustomHelp -title "Help: GiD-Impact Interface" -start 1.About_Interface.html} \
             "" "ArrowRight.gif" "insert"
     GiDMenu::InsertOption   Impact-Help  [list   "GiD to Impact Manual"]  1 PREPOST \
             {GiDCustomHelp -title "Help: GiD-Impact Interface" -start 2.Interface_Manual.html} \
             "" "ArrowRight.gif" "insert"
     GiDMenu::InsertOption   Impact-Help  [list   "Impact Users Manual"]  2 PREPOST \
             {GiDCustomHelp -title "Help: GiD-Impact Interface" -start 3.Impact_Manual.html} \
             "" "ArrowRight.gif" "insert"
     GiDMenu::InsertOption   Impact-Help  [list   "Example Problems"]  3 PREPOST \
             {GiDCustomHelp -title "Help: GiD-Impact Interface" -start 4.Example_Problems.html} \
             "" "ArrowRight.gif"
     GiDMenu::UpdateMenus
}

