/*
 *  Copyright (C) 2011 Axel Morgner
 *
 *  This file is part of structr <http://structr.org>.
 *
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

var buttonClicked;

var _Entities = {
    
    changeBooleanAttribute : function(attrElement, value) {

        if (debug) console.log('Change boolean attribute ', attrElement, ' to ', value);

        if (value == true) {
            attrElement.removeClass('disabled');
            attrElement.addClass('enabled')
        } else {
            attrElement.removeClass('enabled');
            attrElement.addClass('disabled')
        }

    },

    renderTree : function(parent, rootId) {
        if (debug) console.log('Entities.renderTree');
        var children = parent.children;
					
        if (children && children.length > 0) {
            $(children).each(function(i,child) {
                if (debug) console.log(child);
                if (child.type == "Resource") {
                    _Resources.appendResourceElement(child, parent.id, rootId);
                } else if (child.type == "Component") {
                    _Resources.appendComponentElement(child, parent.id, rootId);
                } else if (child.type == "Content") {
                    _Resources.appendContentElement(child, parent.id, rootId);
                } else if (child.type == "Folder") {
                    var entity = child;
                    console.log('Render Tree: ' , entity);
                    var folderElement = _Files.appendFolderElement(child, parent.id);
                    var files = entity.files;
                    if (files && files.length > 0) {
                        disable($('.delete_icon', folderElement)[0]);
                        $(files).each(function(i, file) {
                            _Files.appendFileElement(file, entity.id);
                        });
                    }
                } else {
                    _Resources.appendElementElement(child, parent.id, rootId);
                }
				
                _Entities.renderTree(child, rootId);
            });
        }
    },

    appendObj : function(entity, parentId, resourceId, removeExisting, hasChildren) {

        // Check if object is not already contained
        //var node = Structr.node(entity.id, parentId, resourceId);
        //if (node && node.length > 0) return false;
        
        // Check if parent node is expanded
        //if (resourceId && parentId && (!isExpanded(parentId, null, resourceId))) return false;

        if (entity.type == 'User') {

            return _UsersAndGroups.appendUserElement(entity, parentId, removeExisting, hasChildren);
            
        } else if (entity.type == 'Group') {
            var groupElement = _UsersAndGroups.appendGroupElement(entity, removeExisting, hasChildren);
            var users = entity.users;
            if (users && users.length > 0) {
                disable($('.delete_icon', groupElement)[0]);
                $(users).each(function(i, user) {
                    return _UsersAndGroups.appendUserElement(user, entity.id, removeExisting, hasChildren);
                });
            }
            return false;

        } else if (entity.type == 'Resource') {
            
            return _Resources.appendResourceElement(entity, null, null, false, hasChildren);

        } else if (entity.type == 'Element') {

            return _Resources.appendElementElement(entity, null, null, false, hasChildren);

        } else if (entity.type == 'Component') {

            return _Resources.appendComponentElement(entity, parentId, resourceId, removeExisting, hasChildren);
            //            var componentElement = _Resources.appendComponentElement(entity, parentId, resourceId, removeExisting, hasChildren);
            //            var elements = entity.elements;
            //            if (elements && elements.length > 0) {
            //                disable($('.delete_icon', componentElement)[0]);
            //                $(elements).each(function(i, element) {
            //                    if (element.type == 'Element') {
            //                        return _Resources.appendElementElement(element, entity.id, removeExisting, hasChildren);
            //                    }
            //                });
            //
            //            }
            return false;

        } else if (entity.type == 'Content') {
            return _Resources.appendContentElement(entity, parentId, resourceId, removeExisting, hasChildren);

        } else if (entity.type == 'Folder') {

            var folderElement = _Files.appendFolderElement(entity, removeExisting, hasChildren);

            var folders = entity.folders;
            if (folders && folders.length > 0) {
                disable($('.delete_icon', folderElement)[0]);
                $(folders).each(function(i, folder) {
                    _Files.appendFolderElement(folder, entity.id, removeExisting, hasChildren);
                });
            }
            var images = entity.images;
            if (images && images.length > 0) {
                disable($('.delete_icon', folderElement)[0]);
                $(images).each(function(i, image) {
                    _Files.appendImageElement(image, entity.id, removeExisting, hasChildren);
                });
            }
            var files = entity.files;
            if (files && files.length > 0) {
                disable($('.delete_icon', folderElement)[0]);
                $(files).each(function(i, file) {

                    if (file.type == 'File') { // files comprise images
                        _Files.appendFileElement(file, entity.id, removeExisting, hasChildren);
                    }

                });
            }

        } else if (entity.type == 'Image') {
            if (debug) console.log('Image:', entity);
            _Files.uploadFile(entity);
            return _Files.appendImageElement(entity, parentId, removeExisting, hasChildren);
        } else if (entity.type == 'File') {
            if (debug) console.log('File: ', entity);
            _Files.uploadFile(entity);
            return _Files.appendFileElement(entity, parentId, removeExisting, hasChildren);
        } else {
            if (debug) console.log('Entity: ', entity);
            var elementElement = _Resources.appendElementElement(entity, parentId, resourceId, removeExisting, hasChildren);
            var elem = entity.elements;
            if (elem && elem.length > 0) {
                if (debug) console.log(elem);
                disable($('.delete_icon', elementElement)[0]);
                $(elem).each(function(i, element) {
                    if (elem.type == 'Element') {
                        return _Resources.appendElementElement(element, entity.id, resourceId, removeExisting, hasChildren);
                    } else if (elem.type == 'Content') {
                        return _Resources.appendContentElement(element, entity.id, resourceId, removeExisting, hasChildren);
                    }
                });
            }
            return false;
        }

    },

    deleteNode : function(button, entity) {
        buttonClicked = button;
        if (isDisabled(button)) return;
        console.log('deleteNode');
        Structr.confirmation('Delete ' + entity.type.toLowerCase() + ' \'' + entity.name + '\' <span class="id">' + entity.id + '</span>?',
            function() {
                if (Command.deleteNode(entity.id)) {
                    $.unblockUI({
                        fadeOut: 25
                    });
                }
            });
    },

    hideProperties : function(button, entity, element) {
        enable(button, function() {
            _Entities.showProperties(button, entity, element);
        });
        element.find('.sep').remove();
        element.find('.props').remove();
    },

    showProperties : function(button, entity, dialog) {

        var views;
	    
        if (entity.type == 'Content') {
            views = ['all', 'in', 'out' ];
        } else {
            views = ['all', 'in', 'out', '_html_'];
        }

        dialog.empty();
        Structr.dialog('Edit Properties of ' + entity.id,
            function() {
                return true;
            },
            function() {
                return true;
            }
            );

        //        if (isDisabled(button)) return;
        //        disable(button, function() {
        //            _Entities.hideProperties(button, entity, view, dialog);
        //        });

        dialog.append('<div id="tabs"><ul></ul>');

        $(views).each(function(i, view) {
            var tabs = $('#tabs', dialog);

            var tabText;
            if (view == 'all') {
                tabText = 'Node';
            } else if (view == '_html_') {
                tabText = 'HTML';
            } else if (view == 'in') {
                tabText = 'Incoming';
            } else if (view == 'out') {
                tabText = 'Outgoing';
            } else {
                tabText = 'Other';
            }

            $('ul', tabs).append('<li class="' + (view == 'all' ? 'active' : '') + '" id="tab-' + view + '">' + tabText + '</li>');

            tabs.append('<div id="tabView-' + view + '"><br></div>');

            var tab = $('#tab-' + view);

            tab.on('click', function() {
                var self = $(this);
                tabs.children('div').hide();
                $('li', tabs).removeClass('active');
                $('#tabView-' + view).show();
                self.addClass('active');
            });

            var tabView = $('#tabView-' + view);
            if (view != 'all') tabView.hide();


            var headers = {};
            headers['X-StructrSessionToken'] = token;
            if (debug) console.log('headers', headers);
            if (debug) console.log('showProperties URL: ' + rootUrl + entity.id + (view ? '/' + view : ''), headers);
            
            $.ajax({
                url: rootUrl + entity.id + (view ? '/' + view : '') + '?pageSize=10',
                async: true,
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                headers: headers,
                success: function(data) {
                    //element.append('<div class="sep"></div>');
                    //element.append('<table class="props"></table>');
                    if (debug) console.log(data.result);
                    $(data.result).each(function(i, res) {
			
                        var keys = Object.keys(res);

                        if (debug ) console.log('keys', keys);

                        //			if (view == 'in' || view == 'out') {
                        //			    tabView.append('<br><h3>Relationship ' + res['id']+ '</h3>')
                        //			}
				
                        tabView.append('<table class="props ' + view + '_' + res['id'] +'"></table>');

                        var props = $('.props.' + view + '_' + res['id'], tabView);
				
                        $(keys).each(function(i, key) {

                            if (view == '_html_') {
                                props.append('<tr><td class="key">' + key.replace(view, '') + '</td><td class="value ' + key + '_">' + formatValue(key, res[key]) + '</td></tr>');
                            } else if (view == 'in' || view == 'out') {
                                props.append('<tr><td class="key">' + key + '</td><td class="value ' + key + '_">' + formatValue(key, res[key]) + '</td></tr>');
                            } else {
                                props.append('<tr><td class="key">' + formatKey(key) + '</td><td class="value ' + key + '_">' + formatValue(key, res[key]) + '</td></tr>');
                            }

                        });

                        $('.props tr td.value input', dialog).each(function(i,v) {
                            var input = $(v);
                            var oldVal = input.val();

                            input.on('focus', function() {
                                input.addClass('active');
                            });

                            input.on('change', function() {
                                input.data('changed', true);
                                _Resources.reloadPreviews();
                            });

                            input.on('focusout', function() {
                                Command.setProperty(entity.id, input.attr('name'), input.val());
                                input.removeClass('active');
                                input.parent().children('.icon').each(function(i, img) {
                                    $(img).remove();
                                });
                            });

                        });
                    });

                }
            });
            debug = false;
        });

    //$( "#tabs" ).tabs();

    },

    appendAccessControlIcon: function(parent, entity, hide) {

        var keyIcon = $('.key_icon', parent);
        var newKeyIcon = '<img title="Access Control and Visibility" alt="Access Control and Visibility" class="key_icon button" src="' + Structr.key_icon + '">';
        if (!(keyIcon && keyIcon.length)) {
            parent.append(newKeyIcon);
            keyIcon = $('.key_icon', parent)
            if (hide) keyIcon.hide();
            keyIcon.on('click', function(e) {
                e.stopPropagation();
                Structr.dialog('Access Control and Visibility', function() {}, function() {});
                var dt = $('#dialogBox .dialogText');

                dt.append('<h3>Owner</h3><p class="nodeSelectBox" id="ownersBox"></p>');
                Command.getProperty(entity.id, 'ownerId', '#ownersBox');

                dt.append('<h3>Visibility</h3><div class="' + entity.id + '_"><button class="switch disabled visibleToPublicUsers_">Public (visible to anyone)</button><button class="switch disabled visibleToAuthenticatedUsers_">Authenticated Users</button></div>');
                var publicSwitch = $('.visibleToPublicUsers_');
                var authSwitch = $('.visibleToAuthenticatedUsers_');

                Command.getProperty(entity.id, 'visibleToPublicUsers', '#dialogBox');
                Command.getProperty(entity.id, 'visibleToAuthenticatedUsers', '#dialogBox');

                if (debug) console.log(publicSwitch);
                if (debug) console.log(authSwitch);

                publicSwitch.on('click', function() {
                    if (debug) console.log('Toggle switch', publicSwitch.hasClass('disabled'))
                    Command.setProperty(entity.id, 'visibleToPublicUsers', publicSwitch.hasClass('disabled'));
                });

                authSwitch.on('click', function() {
                    if (debug) console.log('Toggle switch', authSwitch.hasClass('disabled'))
                    Command.setProperty(entity.id, 'visibleToAuthenticatedUsers', authSwitch.hasClass('disabled'));
                });

            });
        
            keyIcon.on('mouseover', function(e) {
                var self = $(this);
                self.show();

            });
        }
    },

    appendEditPropertiesIcon : function(parent, entity) {

        //	if (entity.type != 'Content') {
        //	    el.append('<span class="_html_id_">#</span> <span class="_html_class_">.</span>');
        //	}

        //_Entities.getProperty(entity.id, '_html_id');
        //	_Entities.getProperty(entity.id, '_html_class');

        var editIcon = $('.edit_props_icon', parent);

        if (!(editIcon && editIcon.length)) {
            parent.append('<img title="Edit Properties" alt="Edit Properties" class="edit_props_icon button" src="' + '/structr/icon/application_view_detail.png' + '">');
            editIcon = $('.edit_props_icon', parent)
        }
        editIcon.on('click', function(e) {
            e.stopPropagation();
            if (debug) console.log('showProperties', entity);
            _Entities.showProperties(this, entity, $('#dialogBox .dialogText'));
        });

    //	$('b', el).on('click', function(e) {
    //	    e.stopPropagation();
    //	    _Entities.showProperties(this, entity, $('#dialogBox .dialogText'));
    //	});

    },

    //    setClick : function(el) {
    //	el.on('click', function(e) {
    //	    e.stopPropagation();
    //	    var resourceId = getId($(this).closest('.resource'));
    //	    console.log('Clicked on element', el, resourceId);
    //	    var id = getId(el);
    //	    _Entities.children(id, resourceId);
    //	});
    //    },
    appendExpandIcon : function(el, entity, hasChildren) {

        if (hasChildren) {

            el.append('<img title="Expand \'' + entity.name + '\'" alt="Expand \'' + entity.name + '\'" class="expand_icon" src="' + Structr.expand_icon + '">');
            var button = $('.expand_icon', el).first();

            if (button) {

                button.on('click', function() {
                    _Entities.toggleElement(this);
                });

                // Prevent expand icon from being draggable
                button.on('mousedown', function(e) {
                    e.stopPropagation();
                });

                if (debug) console.log('appendExpandIcon', isExpanded(entity.id, null, entity.resourceId), entity);
                if (isExpanded(entity.id, null, entity.resourceId)) {
                    if (debug) console.log('toggle', entity.id, entity.resourceId);
                    _Entities.toggleElement(button)
                }
            }

        } else {
            el.children('.typeIcon').css({
                paddingRight: 11 + 'px'
            });
        }

    },

    setMouseOver : function(el) {
        if (!el || !el.children) return;

        el.on('click', function(e) {
            e.stopPropagation();
        });

        el.children('b.name_').on('click', function() {
            _Entities.makeNameEditable(el);
        });

        el.on({
            mouseover: function(e) {
                e.stopPropagation();
                var self = $(this);
                var nodeId = getId(el);
                //window.clearTimeout(timer[nodeId]);
                //		console.log('setMouseOver', nodeId);
                var nodes = $('.' + nodeId + '_');
                var resource = $(el).closest('.resource');
                if (resource.length) {
                    var resId = getId(resource);
                    //console.log('setMouseOver resourceId', resId);
                    var previewNodes = $('#preview_' + resId).contents().find('[structr_element_id]');
                    previewNodes.each(function(i,v) {
                        var self = $(v);
                        var sid = self.attr('structr_element_id');
                        if (sid == nodeId) {
                            self.addClass('nodeHover');
                        }
                    });

                //		    _Entities.children(nodeId, resId);

                }
                nodes.addClass('nodeHover');
                self.children('img.button').show();
            },
            mouseout: function(e) {
                e.stopPropagation();
                _Entities.resetMouseOverState(this);
            }
        });
    },

    resetMouseOverState : function(element) {
        var el = $(element);
        el.removeClass('nodeHover');
        el.children('img.button').hide();
        var nodeId = getId(el);
        var nodes = $('.' + nodeId + '_');
        //	timer[nodeId] = window.setTimeout(function() {
        //	    el.children('.node').remove();
        //	}, 1000);
        nodes.removeClass('nodeHover');
        var resource = $(el).closest('.resource');
        if (resource.length) {
            var resId = getId(resource);
            //		    console.log('setMouseOver resourceId', resId);
            var previewNodes = $('#preview_' + resId).contents().find('[structr_element_id]');
            previewNodes.each(function(i,v) {
                var self = $(v);
                var sid = self.attr('structr_element_id');
                if (sid == nodeId) {
                    if (debug) console.log(sid);
                    self.removeClass('nodeHover');
                    if (debug) console.log(self);
                }
            });
        }
    },

    toggleElement : function(button) {

        var b = $(button);
        var src = b.attr('src');

        if (!src) return;

        var nodeElement = $(button).parent();
        var id = getId(nodeElement);
        var resId = getId(nodeElement.closest('.resource'));

        if (src.endsWith('icon/tree_arrow_down.png')) {
            nodeElement.children('.node').remove();
            b.attr('src', 'icon/tree_arrow_right.png');

            removeExpandedNode(id, null, resId);
        } else {
            Command.children(id, resId);
            b.attr('src', 'icon/tree_arrow_down.png');

            addExpandedNode(id, null, resId);
        }

    },

    makeNameEditable : function(element) {
        //element.off('dblclick');
        element.off('hover');
        var oldName = $.trim(element.children('b.name_').text());
        //console.log('oldName', oldName);
        element.children('b').replaceWith('<input type="text" size="' + (oldName.length+4) + '" class="newName_" value="' + oldName + '">');
        element.find('.button').hide();

        var input = $('input', element);

        input.focus().select();

        input.on('blur', function() {
            var self = $(this);
            var newName = self.val();
            Command.setProperty(getId(element), "name", newName);
            self.replaceWith('<b class="name_">' + newName + '</b>');
            $('.name_', element).on('click', function() {
                _Entities.makeNameEditable(element);
            });
            _Resources.reloadPreviews();
        });

        input.keypress(function(e) {
            if (e.keyCode == 13) {
                var self = $(this);
                var newName = self.val();
                Command.setProperty(getId(element), "name", newName);
                self.replaceWith('<b class="name_">' + newName + '</b>');
                $('.name_', element).on('click', function() {
                    _Entities.makeNameEditable(element);
                });
                _Resources.reloadPreviews();
            }
        });

        element.off('click');

    }


};
