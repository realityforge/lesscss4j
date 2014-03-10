/*
   Copyright 2010-present Local Matters, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.localmatters.lesscss4j.model;

import java.util.ArrayList;
import java.util.List;

public class Media extends BodyElementContainer implements BodyElement {
    private List<String> _mediums = new ArrayList<>();

    public Media() {
    }


    public Media( final Media copy) {
        this(copy, true);
    }

    public Media( final Media copy, final boolean copyBodyElements) {
        super(copy, copyBodyElements);
        _mediums.addAll(copy._mediums);
    }

    public List<String> getMediums() {
        return _mediums;
    }

    public void setMediums( final List<String> mediums) {
        _mediums = mediums;
        if (_mediums == null) {
            _mediums = new ArrayList<>();
        }
    }

    public void addMedium( final String medium) {
        if (_mediums == null) {
            _mediums = new ArrayList<>();
        }
        _mediums.add(medium);
    }

    @Override
    public Object clone()  {
        return new Media(this);
    }
}
